(ns calendario.calendar
  (:require [clj-icalendar.core :as ical]
            [clojure.tools.logging :refer [error warn debug]]))

(defn package-lob?
  "given a trip and a line of business (LOB) keyword,
   return true if that LOB is contained in the package"
  [lob trip]
  (->> (get-in trip [:responseData :packages])
       first
       lob
       count
       pos?))

(def package-hotel?
  (partial package-lob? :hotels))
(def package-flight?
  (partial package-lob? :flights))
(def package-car?
  (partial package-lob? :cars))
(def package-activity?
  (partial package-lob? :activities))

(defn flight?
  "check if a trip contains a flight"
  [trip]
  (or (pos? (count (get-in trip [:responseData :flights])))
      (package-flight? trip)))

(defn hotel?
  "check if a trip contains a hotel"
  [trip]
  (or (pos? (count (get-in trip [:responseData :hotels])))
      (package-hotel? trip)))

(defn car?
  "check if a trip contains a car"
  [trip]
  (or (pos? (count (get-in trip [:responseData :cars])))
      (package-car? trip)))

(defn activity?
  "check if a trip contains a activity"
  [trip]
  (or (pos? (count (get-in trip [:responseData :activities])))
      (package-activity? trip)))

(defn cruise?
  "check if a trip contains a cruise"
  [trip]
  (pos? (count (get-in trip [:responseData :cruises]))))

(defn cruise-details
  "return a string suitable for an event calendar
   describing the details of a cruise booking"
  [cruise url]
  (let [start-time (:departureDate cruise)
        end-time (:returnDate cruise)
        cruise-line (:cruiseLineName cruise)
        ship-name (:shipName cruise)
        conf-num (:cruiseLineBkgConfNumber cruise)
        title (:title cruise)]
    (str "Your Cruise: " title "\r\n"
         "Start Time:  " start-time "\r\n"
         "End Time: " end-time "\r\n"
         "Cruise Line: " cruise-line "\r\n"
         "Ship Name: " ship-name "\r\n"
         (when conf-num (str "Confirmation Number: " conf-num "\r\n\r\n"))
         "View the full details of your booking at " url "\r\n\r\n")))

(defn create-cruise-event [cruise itin-num url title]
  { :url url
   :event-type :cruise
   :title title
   :start (:departureDate cruise)
   :end   (:returnDate cruise)
   :location title
   :details (cruise-details cruise url)})

(defn create-cruise-events [cruises itin-num url title]
  (map #(create-cruise-event % itin-num url title) cruises))

(defn cruise-standalone-event [trip]
  (create-cruise-events  (get-in trip [:responseData :cruises])
                         (get-in trip [:responseData :tripNumber])
                (get-in trip [:responseData :webDetailsURL])
                (get-in trip [:responseData :title])))

(defn cruises
  "given a trip, return a list of events for cruise bookings.
  each event should contain start-date, end-date, url to the trip,
  and a description."
  [trip]
  (when (cruise? trip)
    (if (seq (get-in trip [:responseData :cruises]))
      (cruise-standalone-event trip))))

(defn activity-location [activity]
  (str  (get-in activity [:activityLocation :fullAddress]) "@"
        (get-in activity [:activityLocation :latitude]) ","
        (get-in activity [:activityLocation :longitude])))

(defn activity-details
  "return a string suitable for an event calendar
   describing the details of a activity booking"
  [activity url event-type]
  (let [supplier-ref-num (:supplierReferenceNumber activity)
        title (:activityTitle activity)
        location (activity-location activity)
        vendor (:vendorName activity)]
    (str (if (= :activity-start event-type)
           "Activity Start Time\r\n"
           "Activity End Time\r\n")
         "Vendor: " vendor "\r\n"
         "Supplier Ref Num: " supplier-ref-num  "\r\n"
         "Location: " location "\r\n\r\n"
         "View the full details of your booking at " url "\r\n\r\n")))

(defn create-activity-event
  "create one activity event. some activities have reasonable start and
   end datetimes (e.g. 2 hours for yoga), others are problematic (e.g.
   redeem your voucher for bus tour anytime from May 11 to June 19).
   The latter type of event does not lend itself well to calendar events."
  [activity itin-num url title event-type]
  { :url url
   :itin-num itin-num
   :event-type event-type
   :title title
   :start (get-in activity [:startTime :epochSeconds])
   :end   (get-in activity [:endTime :epochSeconds])
   :location (activity-location activity)
   :details (activity-details activity url event-type)})

(defn create-activity-events
  "create one event for car pickup and one for car dropoff"
  [activity itin-num url title]
  [(create-activity-event activity itin-num url title :activity-start)
   (create-activity-event activity itin-num url title :activity-end)])

(defn activity-standalone-event [trip]
  (let [activities (get-in trip [:responseData :activities])]
    (mapcat #(create-activity-events %
                                (get-in trip [:responseData :tripNumber])
                                (get-in trip [:responseData :webDetailsURL])
                                (get-in trip [:responseData :title])) activities)))

(defn activity-package-event [trip]
  (let [activities (->> (get-in trip [:responseData :packages])
                  first
                  :activities) ]
    (mapcat  #(create-activity-events %
                                 (get-in trip [:responseData :tripNumber])
                                 (get-in trip [:responseData :webDetailsURL])
                                 (get-in trip [:responseData :title])) activities)))

(defn activities
  "given a trip, return a list of events for activity bookings.
  each event should contain start-date, end-date, url to the trip,
  and a description."
  [trip]
  (when (activity? trip)
    (if (seq (get-in trip [:responseData :activities]))
      (activity-standalone-event trip)
      (activity-package-event trip))))

(defn car-location [car event-type]
  (let [loc (if (= event-type :dropoff) (:dropOffLocation car) (:pickupLocation car))
        airport-instructions (:airportInstructions loc)]
    (str  (:locationDescription loc) ", "
          (if (seq airport-instructions) (str " (" airport-instructions ") ") "")
          (:addressLine1 loc) ", "
          (:cityName loc) " "
          (:countryCode loc) "@"
          (:latitude loc) ","
          (:longitude loc))))

(defn car-details
  "return a string suitable for an event calendar
   describing the details of a car booking"
  [car url event-type]
  (let [loc (if (= event-type :car-dropoff) (:dropOffLocation car) (:pickupLocation car))
        vendor (get-in car [:carVendor :longName])
        car-category (:carCategory car)
        car-type (:carType car)
        confirmation-number (:confirmationNumber car)
        pick-up-instructions (apply str (:pickUpInstructions car))]
    (str  (if (= :car-pickup event-type)
            "Car Pick Up Time.\r\n"
            "Car Drop Off Time.\r\n")
          "Vendor: " vendor "\r\n"
          "Car: " car-category " " car-type " \r\n"
          "Confirmation Number: " confirmation-number "\r\n"
          (when (= :car-pickup event-type) (str "Pick Up Instructions: " pick-up-instructions "\r\n"))
          "Location: " (car-location car event-type) "\r\n\r\n"
          "View the full details of your booking at " url  "\r\n\r\n")))

(defn create-car-event [car itin-num url title event-type]
  { :url url
   :itin-num itin-num
   :event-type event-type
   :title title
   :start (get-in car [:pickupTime :epochSeconds])
   :end   (get-in car [:dropOffTime :epochSeconds])
   :location (car-location car event-type)
   :details (car-details car url event-type)})

(defn create-car-events
  "create one event for car pickup and one for car dropoff"
  [car itin-num url title]
  [(create-car-event car itin-num url title :car-pickup)
   (create-car-event car itin-num url title :car-dropoff)])

(defn car-standalone-event [trip]
  (let [cars (get-in trip [:responseData :cars])]
    (mapcat #(create-car-events %
                                (get-in trip [:responseData :tripNumber])
                                (get-in trip [:responseData :webDetailsURL])
                                (get-in trip [:responseData :title])) cars)))

(defn car-package-event [trip]
  (let [cars (->> (get-in trip [:responseData :packages])
                  first
                  :cars) ]
    (mapcat  #(create-car-events %
                                 (get-in trip [:responseData :tripNumber])
                                 (get-in trip [:responseData :webDetailsURL])
                                 (get-in trip [:responseData :title])) cars)))

(defn cars
  "given a trip, return a list of events for car bookings.
  each event should contain start-date, end-date, url to the trip,
  and a description."
  [trip]
  (when (car? trip)
    (if (seq (get-in trip [:responseData :cars]))
      (car-standalone-event trip)
      (car-package-event trip))))

(defn hotel-details
  "return a string suitable for an event calendar
   describing the details of the hotel booking"
  [hotel url event-type]
  (let [hotel-name (get-in hotel [:hotelPropertyInfo :name])
        hotel-address (get-in hotel [:hotelPropertyInfo :address :fullAddress])
        hotel-phone (get-in hotel [:hotelPropertyInfo :localPhone])
        gmap-link (get-in hotel [:hotelPropertyInfo :googleMapsLink])
        lat (get-in hotel [:hotelPropertyInfo :latitude])
        lon (get-in hotel [:hotelPropertyInfo :longitude])]
    (format (str (if (= :hotel-checkin event-type)
                   "Check in Time\r\n" "Check Out time\r\n")
                 "Hotel Name: %s\r\n"
                 "Hotel Address:  %s@%s,%s\r\n"
                 "Hotel Phone: %s\r\n\r\n"
                 "View the full details of your booking at %s\r\n\r\n"
                 "Map: %s") hotel-name hotel-address lat lon hotel-phone url gmap-link)))

(defn create-hotel-event
  "generate one event that should not block calendar for the entire stay "
  [hotel itin-num url title event-type]
  (let [hotel-address (get-in hotel [:hotelPropertyInfo :address :fullAddress])
        lat (get-in hotel [:hotelPropertyInfo :latitude])
        lon (get-in hotel [:hotelPropertyInfo :longitude])]
    { :url url
     :itin-num itin-num
     :event-type event-type
     :title title
     :start (get-in hotel [:checkInDateTime :epochSeconds])
     :end   (get-in hotel [:checkOutDateTime :epochSeconds])
     :location (format "%s@%s,%s" hotel-address lat lon)
     :details (hotel-details hotel url event-type)
     }))

(defn create-hotel-events
  "create one event for hotel checkin  and one for hotel checkout"
  [hotel itin-num url title]
  [(create-hotel-event hotel itin-num url title :hotel-checkin)
   (create-hotel-event hotel itin-num url title :hotel-checkout)])

(defn hotel-standalone-events [trip]
  (let [hotels (get-in trip [:responseData :hotels])]
    (mapcat #(create-hotel-events %
                                (get-in trip [:responseData :tripNumber])
                                (get-in trip [:responseData :webDetailsURL])
                                (get-in trip [:responseData :title])) hotels)))

(defn hotel-package-events [trip]
  (let [hotels (->> (get-in trip [:responseData :packages])
                  first
                  :hotels) ]
    (mapcat  #(create-hotel-events %
                                 (get-in trip [:responseData :tripNumber])
                                 (get-in trip [:responseData :webDetailsURL])
                                 (get-in trip [:responseData :title])) hotels)))

(defn hotels
  "given a trip, return a list of events for the hotel bookings.
  each event should contain start-date, end-date, url to the trip,
  and a description."
  [trip]
  (when (hotel? trip)
    (if (seq (get-in trip [:responseData :hotels]))
      (hotel-standalone-events trip)
      (hotel-package-events trip))))

(defn airline-name
  "fn takes 2 arguments, only one will be populated. If airline name is not
   blank, then use it. Otherwise use operatedbyaircarriername. "
  [{:keys [airlineName operatedByAirCarrierName]}]
  (first (remove clojure.string/blank? [airlineName operatedByAirCarrierName])))

(defn flight-details
  "return a string suitable for an event calendar
   describing the details of a flight segment"
  [segment url passengers]
  (let [airline-name (airline-name segment)
        depart-name (get-in segment [:departureLocation :name])
        arrive-name (get-in segment [:arrivalLocation :name])
        depart-code (get-in segment [:departureLocation :airportCode])
        arrive-code (get-in segment [:arrivalLocation :airportCode])]
    (format (str "Airline:  %s\r\n"
                 "Flight Number: %s\r\n"
                 "%s (%s) to %s (%s)\r\n"
                 (if (seq (:confirmationNumber segment)) (str "Confirmation Number: " (:confirmationNumber segment)  "\r\n"))
                 "Passengers: %s\r\n\r\n"
                 "View the full details of your booking at %s\r\n\r\n")
            airline-name
            (:flightNumber segment)
            depart-name depart-code
            arrive-name arrive-code
            ;(:confirmationNUmber segment)
            (apply str (interpose ", " (map :fullName passengers)))
            url
            )))

(defn create-flight-event
  "create one event with start time / end time"
  [segment itin-num url passengers]
  (let [depart-name (get-in segment [:departureLocation :name])
        arrive-name (get-in segment [:arrivalLocation :name])
        lat (get-in segment [:departureLocation :latitude])
        lon (get-in segment [:departureLocation :longitude])
        depart-code (get-in segment [:departureLocation :airportCode])
        arrive-code (get-in segment [:arrivalLocation :airportCode])]
    { :url url
     :itin-num itin-num
     :event-type :flight
     :title (format "Flight: %s %s From %s To %s" (airline-name segment) (:flightNumber segment) depart-name arrive-name )
     :start (get-in segment [:departureTime :epochSeconds])
     :end   (get-in segment [:arrivalTime :epochSeconds])
     :location (format "%s (%s)@%s,%s" depart-name depart-code lat lon)
     :details (flight-details segment url passengers)
     }))

(defn create-flight-events [segments itin-num url passengers ]
  (map #(create-flight-event % itin-num url passengers) segments))

(defn flight-standalone-event [trip]
  (let [flights (get-in trip [:responseData :flights])]
    (mapcat  #(create-flight-events
            (mapcat :segments (:legs %))
            (get-in trip [:responseData :tripNumber])
           (get-in trip [:responseData :webDetailsURL])
           (:passengers %)) flights)))

(defn flight-package-event [trip]
  (let [flights (->> (get-in trip [:responseData :packages])
                     first
                     :flights)]
    (mapcat  #(create-flight-events
               (mapcat :segments (:legs %))
               (get-in trip [:responseData :tripNumber])
               (get-in trip [:responseData :webDetailsURL])
               (:passengers %)) flights)))

(defn flights
  "given a trip, return a list of events for the flights in the booking.
  each event should contain start-date, end-date, url to the trip,
  and a description. One event should be generated for
  the beginning of each flight segment."
  [trip]
  (when (flight? trip)
    (if (seq (get-in trip [:responseData :flights]))
      (flight-standalone-event trip)
      (flight-package-event trip))))

(defn create-events-for-trip
  "given a json trip, extract the events for each line of business.
   cruise is not included because the start date is in the wrong format"
  [json-trip]
  (mapcat seq ((juxt flights hotels cars activities) json-trip)))

#_(defn create-events-from-trips [json-trips]
  (mapcat seq
          (mapcat (juxt flights hotels cars activities)
                  json-trips)))

#_(defn create-events-from-trips [tuid site-id cm trip-numbers]
  (let [trip-f (partial get-trip-for-user tuid site-id cm)
        json-trips (remove nil?  (map trip-f trip-numbers))
        _ (debug "For user " tuid " site: " site-id " upcoming trips:" (count trip-numbers) " successfully read: " (count json-trips))]
    (mapcat seq
            (mapcat (juxt flights hotels cars activities)
                    json-trips))))

; flight - event with duration
; hotel event no duration checkin
; car  2 events no duration, pickup dropoff
; activity - event with duration
; cruise - event with duration
; add no-duration event for trip start?
; add reminders via api - num minutes before event
(defn create-ical-event [e i]
  (let [start (java.util.Date. (* 1000 (.longValue (:start e))))
        end (java.util.Date. (* 1000 (.longValue (:end e))))
        title (:title e)
        unique-id (str (:itin-num e) "_" i "@" (quot (System/currentTimeMillis) 1000))
        desc (:details e)
        url (:url e)
        location (:location e)]
    (condp = (:event-type e)
      :flight  (ical/create-event start end title
                                  :unique-id unique-id
                                  :description desc
                                  :url url
                                  :location location
                                  :organizer url)
      :hotel-checkin  (ical/create-event-no-duration start title
                                                     :unique-id unique-id
                                                     :description desc :url url
                                                     :location location
                                                     :organizer url)
      :hotel-checkout  (ical/create-event-no-duration end title
                                                      :unique-id unique-id
                                                      :description desc
                                                      :url url
                                                      :location location
                                                      :organizer url)
      :car-pickup (ical/create-event-no-duration start title
                                                 :unique-id unique-id
                                                 :description desc
                                                 :url url
                                                 :location location
                                                 :organizer url)
      :car-dropoff (ical/create-event-no-duration end title
                                                  :unique-id unique-id
                                                  :description desc
                                                  :url url
                                                  :location location
                                                  :organizer url)
      :activity-start  (ical/create-event-no-duration start title
                                                      :unique-id unique-id
                                                      :description desc
                                                      :url url
                                                      :location location
                                                      :organizer url)
      :activity-end  (ical/create-event-no-duration end title
                                                    :unique-id unique-id
                                                    :description desc
                                                    :url url
                                                    :location location
                                                    :organizer url)
      :cruise  (ical/create-event start end title
                                  :unique-id unique-id
                                  :description desc
                                  :url url
                                  :location location
                                  :organizer url))))

(defn create-ical
  "create an ical object given a collection of events.
   Each event is meant to have specific keys:
   start and end a date with format 2015-08-31T15:59:07-04:00
   url is url of the event. location a text description,
   details, a description of the event. title is the event title
   event-type can be flight hotel car activity cruise"
  [events]
  (let [cal  (ical/create-cal "Expedia, Inc." "Trip Calendar" "V0.1" "EN")
        ical-events (map #(create-ical-event % %2) events (range (count events)))]
    (reduce (fn [c e] (ical/add-event! c e)) cal ical-events)
    cal))

(def empty-cal "BEGIN:VCALENDAR
  PRODID:-//Expedia, Inc. //Trip Calendar V0.1//EN
  VERSION:2.0
  METHOD:PUBLISH
  CALSCALE:GREGORIAN
  END:VCALENDAR")

#_(defn calendar-from-json-trips [json-trips]
  (->> json-trips
       create-events-from-trips
       create-ical
       ical/output-calendar))

(defn calendar-from-events [events]
  (if (seq events)
    (->> events
         create-ical
         ical/output-calendar)
    (do  (debug "cannot create calendar with no events")
         empty-cal)))

#_(defn cal-for [tuid siteid cm]
  (->> (get-booked-upcoming-trips tuid siteid cm)
       (create-events-from-trips tuid siteid cm)
       (create-ical)))


#_(time (def jeffcal (create-ical (create-events-file-based file-based-trips))))
