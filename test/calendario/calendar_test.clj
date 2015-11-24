(ns calendario.calendar-test
  (:require [clojure.test :refer :all]
            [calendario.calendar :refer :all]
            [cheshire.core :refer [parse-string]]))

(deftest line-of-business-present
  (testing "check for presence of line of business"
    (is (= true (package-hotel? {:responseData {
                                                :packages
                                                [{:hotels [{:hotelId 1234}]}]}})))
    (is (= true (package-flight? {:responseData {:packages [{:flights [{:bookingStatus "BOOKED"}]}]}})))
    (is (= true (package-car? {:responseData {:packages [{:cars [{:bookingStatus "BOOKED"}]}]} })))
    (is (= true (package-activity? {:responseData {:packages [{:activities [{:bookingStatus "BOOKED"}]}]} } )))
    (is (= true (flight? {:responseData {:flights [{:bookingStatus "BOOKED"}]} } )))
    (is (= true (hotel? {:responseData {:hotels [{:bookingStatus "BOOKED"}]} } )))
    (is (= true (car? {:responseData {:cars [{:bookingStatus "BOOKED"}]} } )))
    (is (= true (activity? {:responseData {:activities [{:bookingStatus "BOOKED"}]} } )))
    (is (= true (cruise? {:responseData {:cruises [{:bookingStatus "BOOKED"}]} } )))
    ))

(def file-based-trips [11613235441 11616639117 7822022514
                       11616506271 11616643514 7822024975
                       11616506364 11617273915 7822027698
                       1115913015028 7828048848 7834616605
                       11624796803 11624954701 11624965146
                       7835454236 11624970813 11625930503])

(defn build-file-name
  "take a trip number and add a prefix and suffix
   in order to look it up in the resources dir"
  [itinerary-number]
  (str "trips/" itinerary-number ".json"))

(defn read-trip-from-file
  "accept a relative filepath argument on the classpath
  ,then slurp the file up and parse the json into clojure"
  [file-name]
  (let [f (slurp (clojure.java.io/resource file-name))]
    (parse-string f true)))

(def a-cruise-event '({:details "Your Cruise: \r\nStart Time:  Oct 2, 2016 4:00 PM\r\nEnd Time: Oct 9, 2016 8:00 AM\r\nCruise Line: Norwegian Cruise Line\r\nShip Name: Norwegian Getaway\r\nConfirmation Number: 29225136\r\n\r\nView the full details of your booking at https://wwwexpedia-aarpcom.trunk.sb.karmalab.net/trips/1062225501?email=aalee@expedia.com\r\n\r\n",
                       :end "Oct 9, 2016 8:00 AM",
                       :event-type :cruise,
                       :location "7-night Cruise from MIAMI",
                       :start "Oct 2, 2016 4:00 PM",
                       :title "7-night Cruise from MIAMI",
                       :url "https://wwwexpedia-aarpcom.trunk.sb.karmalab.net/trips/1062225501?email=aalee@expedia.com"}))

(deftest cruises-test
  (testing "events can be created for cruises"
    (let [cruise-trip (-> "7828048848"
                          build-file-name
                          read-trip-from-file)]
      (is (= a-cruise-event (cruises cruise-trip))))))

(def activity-events '({:details "Activity Start Time\r\nVendor: Urban Adventures - CAD\r\nSupplier Ref Num: 11616646637\r\nLocation: Central Park Sightseeing : 56 West 56th Street, near 6th Avenue, New York, New York, 10019, USA@40.76327,-73.97713\r\n\r\nView the full details of your booking at https://wwwexpediacom.integration.sb.karmalab.net/trips/7822024975\r\n\r\n",
  :end 1465455600,
  :event-type :activity-start,
  :itin-num 7822024975,
  :location "Central Park Sightseeing : 56 West 56th Street, near 6th Avenue, New York, New York, 10019, USA@40.76327,-73.97713",
  :start 1462950000,
  :title "New York, United States",
  :url "https://wwwexpediacom.integration.sb.karmalab.net/trips/7822024975"}
 {:details "Activity End Time\r\nVendor: Urban Adventures - CAD\r\nSupplier Ref Num: 11616646637\r\nLocation: Central Park Sightseeing : 56 West 56th Street, near 6th Avenue, New York, New York, 10019, USA@40.76327,-73.97713\r\n\r\nView the full details of your booking at https://wwwexpediacom.integration.sb.karmalab.net/trips/7822024975\r\n\r\n",
  :end 1465455600,
  :event-type :activity-end,
  :itin-num 7822024975,
  :location "Central Park Sightseeing : 56 West 56th Street, near 6th Avenue, New York, New York, 10019, USA@40.76327,-73.97713",
  :start 1462950000,
  :title "New York, United States",
  :url "https://wwwexpediacom.integration.sb.karmalab.net/trips/7822024975"}))


(deftest activities-test
  (testing "events created for activities"
    (let [a (-> "7822024975"
                          build-file-name
                          read-trip-from-file)]
      (is (= activity-events (activities a))))))

(def car-events '({:details "Car Pick Up Time.\r\nVendor: Enterprise\r\nCar: Economy ThreeDoorCar \r\nConfirmation Number: 1801185418COUNT\r\nPick Up Instructions: COUNTER LOCATED IN TERMINAL. CARS ARE WITHIN WALKING\nDISTANCE.\n\r\nLocation: Barcelona (BCN),  (Counter and car in terminal) Aeropuerto De Barcelona., Barcelona ESP@41.303706,2.077613\r\n\r\nView the full details of your booking at https://wwwexpediacom.integration.sb.karmalab.net/trips/7822022514\r\n\r\n",
  :end 1457857800,
  :event-type :car-pickup,
  :itin-num 7822022514,
  :location "Barcelona (BCN),  (Counter and car in terminal) Aeropuerto De Barcelona., Barcelona ESP@41.303706,2.077613",
  :start 1457598600,
  :title "Car rental in Barcelona",
  :url "https://wwwexpediacom.integration.sb.karmalab.net/trips/7822022514"}
 {:details "Car Drop Off Time.\r\nVendor: Enterprise\r\nCar: Economy ThreeDoorCar \r\nConfirmation Number: 1801185418COUNT\r\nLocation: Barcelona (BCN),  (Counter and car in terminal) Aeropuerto De Barcelona., Barcelona ESP@41.303706,2.077613\r\n\r\nView the full details of your booking at https://wwwexpediacom.integration.sb.karmalab.net/trips/7822022514\r\n\r\n",
  :end 1457857800,
  :event-type :car-dropoff,
  :itin-num 7822022514,
  :location "Barcelona (BCN),  (Counter and car in terminal) Aeropuerto De Barcelona., Barcelona ESP@41.303706,2.077613",
  :start 1457598600,
  :title "Car rental in Barcelona",
  :url "https://wwwexpediacom.integration.sb.karmalab.net/trips/7822022514"}))

(deftest cars-test
  (testing "events created for cars"
    (let [c (-> "7822022514"
                build-file-name
                read-trip-from-file)]
      (is (= car-events (cars c))))))

(def hotel-events '({:details "Check in Time\r\nHotel Name: Hilton Tokyo\r\nHotel Address:  6-6-2, Nishi-Shinjuku, Shinjuku-ku, Tokyo, Tokyo-to, 160-0023 Japan@35.69226,139.69159\r\nHotel Phone: 81-33-3445111\r\n\r\nView the full details of your booking at https://wwwexpediacom.integration.sb.karmalab.net/trips/11616643514\r\n\r\nMap: http://maps.google.com?q=160-0023+Tokyo,+6-6-2,+Nishi-Shinjuku,+Shinjuku-ku+",
  :end 1454724000,
  :event-type :hotel-checkin,
  :itin-num 11616643514,
  :location "6-6-2, Nishi-Shinjuku, Shinjuku-ku, Tokyo, Tokyo-to, 160-0023 Japan@35.69226,139.69159",
  :start 1454479200,
  :title "Hilton Tokyo, Tokyo",
  :url "https://wwwexpediacom.integration.sb.karmalab.net/trips/11616643514"}
 {:details "Check Out time\r\nHotel Name: Hilton Tokyo\r\nHotel Address:  6-6-2, Nishi-Shinjuku, Shinjuku-ku, Tokyo, Tokyo-to, 160-0023 Japan@35.69226,139.69159\r\nHotel Phone: 81-33-3445111\r\n\r\nView the full details of your booking at https://wwwexpediacom.integration.sb.karmalab.net/trips/11616643514\r\n\r\nMap: http://maps.google.com?q=160-0023+Tokyo,+6-6-2,+Nishi-Shinjuku,+Shinjuku-ku+",
  :end 1454724000,
  :event-type :hotel-checkout,
  :itin-num 11616643514,
  :location "6-6-2, Nishi-Shinjuku, Shinjuku-ku, Tokyo, Tokyo-to, 160-0023 Japan@35.69226,139.69159",
  :start 1454479200,
  :title "Hilton Tokyo, Tokyo",
  :url "https://wwwexpediacom.integration.sb.karmalab.net/trips/11616643514"}))

(def package-hotel-events '({:details "Check in Time\r\nHotel Name: Flamingo Las Vegas\r\nHotel Address:  3555 Las Vegas Blvd S, Las Vegas, NV, 89109 United States of America@36.115316,-115.172766\r\nHotel Phone: 1-702-733-3111\r\n\r\nView the full details of your booking at https://www.expedia.com/trips/1115913015028\r\n\r\nMap: //maps.google.com?q=89109+Las+Vegas,+3555+Las+Vegas+Blvd+S+",
  :end 1444417200,
  :event-type :hotel-checkin,
  :itin-num 1115913015028,
  :location "3555 Las Vegas Blvd S, Las Vegas, NV, 89109 United States of America@36.115316,-115.172766",
  :start 1444086000,
  :title "Las Vegas",
  :url "https://www.expedia.com/trips/1115913015028"}
 {:details "Check Out time\r\nHotel Name: Flamingo Las Vegas\r\nHotel Address:  3555 Las Vegas Blvd S, Las Vegas, NV, 89109 United States of America@36.115316,-115.172766\r\nHotel Phone: 1-702-733-3111\r\n\r\nView the full details of your booking at https://www.expedia.com/trips/1115913015028\r\n\r\nMap: //maps.google.com?q=89109+Las+Vegas,+3555+Las+Vegas+Blvd+S+",
  :end 1444417200,
  :event-type :hotel-checkout,
  :itin-num 1115913015028,
  :location "3555 Las Vegas Blvd S, Las Vegas, NV, 89109 United States of America@36.115316,-115.172766",
  :start 1444086000,
  :title "Las Vegas",
  :url "https://www.expedia.com/trips/1115913015028"}))

(deftest hotels-test
  (testing "events created for hotels"
    (let [h (-> "11616643514"
                build-file-name
                read-trip-from-file)]
      (is (= hotel-events (hotels h))))
    (let [h (-> "1115913015028"
                build-file-name
                read-trip-from-file)]
      (is (= package-hotel-events (hotels h))))))

(def flight-events '({:details "Airline:  Delta\r\nFlight Number: 5805\r\nSan Francisco Intl. (SFO) to Seattle - Tacoma Intl. (SEA)\r\nPassengers: Elmer Fudd\r\n\r\nView the full details of your booking at https://wwwexpediacom.integration.sb.karmalab.net/trips/11624796803\r\n\r\n",
  :end 1457647140,
  :event-type :flight,
  :itin-num 11624796803,
  :location "San Francisco Intl. (SFO)@37.61594,-122.387996",
  :start 1457639100,
  :title "Flight: Delta 5805 From San Francisco Intl. To Seattle - Tacoma Intl.",
  :url "https://wwwexpediacom.integration.sb.karmalab.net/trips/11624796803"}
 {:details "Airline:  Delta\r\nFlight Number: 5750\r\nSeattle - Tacoma Intl. (SEA) to Denver Intl. (DEN)\r\nPassengers: Elmer Fudd\r\n\r\nView the full details of your booking at https://wwwexpediacom.integration.sb.karmalab.net/trips/11624796803\r\n\r\n",
  :end 1457661900,
  :event-type :flight,
  :itin-num 11624796803,
  :location "Seattle - Tacoma Intl. (SEA)@47.45475,-122.30112",
  :start 1457652600,
  :title "Flight: Delta 5750 From Seattle - Tacoma Intl. To Denver Intl.",
  :url "https://wwwexpediacom.integration.sb.karmalab.net/trips/11624796803"}
 {:details "Airline:  Delta\r\nFlight Number: 5737\r\nDenver Intl. (DEN) to Seattle - Tacoma Intl. (SEA)\r\nPassengers: Elmer Fudd\r\n\r\nView the full details of your booking at https://wwwexpediacom.integration.sb.karmalab.net/trips/11624796803\r\n\r\n",
  :end 1458007380,
  :event-type :flight,
  :itin-num 11624796803,
  :location "Denver Intl. (DEN)@39.849354,-104.672735",
  :start 1457996340,
  :title "Flight: Delta 5737 From Denver Intl. To Seattle - Tacoma Intl.",
  :url "https://wwwexpediacom.integration.sb.karmalab.net/trips/11624796803"}
 {:details "Airline:  Delta\r\nFlight Number: 5704\r\nSeattle - Tacoma Intl. (SEA) to San Francisco Intl. (SFO)\r\nPassengers: Elmer Fudd\r\n\r\nView the full details of your booking at https://wwwexpediacom.integration.sb.karmalab.net/trips/11624796803\r\n\r\n",
  :end 1458020760,
  :event-type :flight,
  :itin-num 11624796803,
  :location "Seattle - Tacoma Intl. (SEA)@47.45475,-122.30112",
  :start 1458012600,
  :title "Flight: Delta 5704 From Seattle - Tacoma Intl. To San Francisco Intl.",
  :url "https://wwwexpediacom.integration.sb.karmalab.net/trips/11624796803"}))

(def package-flight-events '({:details "Airline:  Virgin America\r\nFlight Number: 918\r\nSan Francisco Intl. (SFO) to McCarran Intl. (LAS)\r\nPassengers: Jeffrey Michael Madynski\r\n\r\nView the full details of your booking at https://www.expedia.com/trips/1115913015028\r\n\r\n",
  :end 1444100400,
  :event-type :flight,
  :itin-num 1115913015028,
  :location "San Francisco Intl. (SFO)@37.61594,-122.387996",
  :start 1444095300,
  :title "Flight: Virgin America 918 From San Francisco Intl. To McCarran Intl.",
  :url "https://www.expedia.com/trips/1115913015028"}
 {:details "Airline:  Virgin America\r\nFlight Number: 919\r\nMcCarran Intl. (LAS) to San Francisco Intl. (SFO)\r\nPassengers: Jeffrey Michael Madynski\r\n\r\nView the full details of your booking at https://www.expedia.com/trips/1115913015028\r\n\r\n",
  :end 1444440600,
  :event-type :flight,
  :itin-num 1115913015028,
  :location "McCarran Intl. (LAS)@36.085393,-115.150098",
  :start 1444435200,
  :title "Flight: Virgin America 919 From McCarran Intl. To San Francisco Intl.",
  :url "https://www.expedia.com/trips/1115913015028"}))

(def split-ticket-flight-events '({:details "Airline:  Delta\r\nFlight Number: 5931\r\nLaGuardia (LGA) to O'Hare Intl. (ORD)\r\nPassengers: Elmer Fudd\r\n\r\nView the full details of your booking at https://wwwexpediacom.integration.sb.karmalab.net/trips/11624970813\r\n\r\n",
  :end 1465562700,
  :event-type :flight,
  :itin-num 11624970813,
  :location "LaGuardia (LGA)@40.77429,-73.872035",
  :start 1465553700,
  :title "Flight: Delta 5931 From LaGuardia To O'Hare Intl.",
  :url "https://wwwexpediacom.integration.sb.karmalab.net/trips/11624970813"}
 {:details "Airline:  United\r\nFlight Number: 910\r\nO'Hare Intl. (ORD) to LaGuardia (LGA)\r\nPassengers: Elmer Fudd\r\n\r\nView the full details of your booking at https://wwwexpediacom.integration.sb.karmalab.net/trips/11624970813\r\n\r\n",
  :end 1465747380,
  :event-type :flight,
  :itin-num 11624970813,
  :location "O'Hare Intl. (ORD)@41.976928,-87.904787",
  :start 1465740000,
  :title "Flight: United 910 From O'Hare Intl. To LaGuardia",
  :url "https://wwwexpediacom.integration.sb.karmalab.net/trips/11624970813"}))

(deftest flights-test
  (testing "events created for flights"
    (let [f (-> "11624796803"
                build-file-name
                read-trip-from-file)]
      (is (= flight-events (flights f))))
    (let [f (-> "1115913015028"
                build-file-name
                read-trip-from-file)]
      (is (= package-flight-events (flights f))))
    (let [f (-> "11624970813"
                build-file-name
                read-trip-from-file)]
      (is (= split-ticket-flight-events (flights f))))))

(def standalone-car-events '({:details "Car Pick Up Time.\r\nVendor: Alamo\r\nCar: Compact TwoDoorCar \r\nConfirmation Number: 1203965970COUNT\r\nPick Up Instructions:   SEE SHUTTLE POLICY FOR ARRIVAL INFORMATION.\nDUE TO CONSTRUCTION IN AND AROUND THE AIRPORT\nALLOW EXTRA TIME FOR RENTALS\n.\n************ ALAMO*S RENTAL COUNTER *****************\n   AIRPORT - OHARE INTL\n   COUNTER - CONSOLIDATED ALAMO/NATIONAL COUNTERS\n   LOCATED THROUGHOUT THE AIRPORT IN LOWER LEVEL\n   BAGGAGE CLAIM AREAS. WHEN RENTAL IS PROCESSED\n   PROCEED TO ALAMO/NATIONAL SHUTTLE BUS.\n.\n   WHEN COUNTERS ARE UNMANNED - PROCEED TO SHUTTLE.\n.\n*********** ALAMO*S SHUTTLE SERVICE *****************\n   SHUTTLE - ARRIVES FROM 11PM-7AM. USE COURTESY\n   PHONE AT COUNTER TO CALL FOR SHUTTLE BUS.\n- BOARD ALAMO/NATIONAL SHUTTLE BUS\n- RUNS EVERY 5 MINUTES\n- INTL TERMINAL ARRIVALS USE COURTESY PHONE AND\n  CALL 694-4640 FOR ALAMO/NATIONAL SHUTTLE SERVICE.\n\r\nLocation: Chicago (ORD),  (Shuttle to counter and car) OHARE INTL AIRPORT, Chicago USA@41.979629,-87.904565\r\n\r\nView the full details of your booking at https://wwwexpediacom.integration.sb.karmalab.net/trips/7835454236\r\n\r\n",
  :end 1465732800,
  :event-type :car-pickup,
  :itin-num 7835454236,
  :location "Chicago (ORD),  (Shuttle to counter and car) OHARE INTL AIRPORT, Chicago USA@41.979629,-87.904565",
  :start 1465563600,
  :title "Car rental in Chicago",
  :url "https://wwwexpediacom.integration.sb.karmalab.net/trips/7835454236"}
 {:details "Car Drop Off Time.\r\nVendor: Alamo\r\nCar: Compact TwoDoorCar \r\nConfirmation Number: 1203965970COUNT\r\nLocation: Chicago (ORD),  (Shuttle to counter and car) OHARE INTL AIRPORT, Chicago USA@41.979629,-87.904565\r\n\r\nView the full details of your booking at https://wwwexpediacom.integration.sb.karmalab.net/trips/7835454236\r\n\r\n",
  :end 1465732800,
  :event-type :car-dropoff,
  :itin-num 7835454236,
  :location "Chicago (ORD),  (Shuttle to counter and car) OHARE INTL AIRPORT, Chicago USA@41.979629,-87.904565",
  :start 1465563600,
  :title "Car rental in Chicago",
  :url "https://wwwexpediacom.integration.sb.karmalab.net/trips/7835454236"}))

(def standalone-hotel-events '({:details "Check in Time\r\nHotel Name: Sea Cliff House And Motel\r\nHotel Address:  2 Seacliff Ave, Old Orchard Beach, ME, 04064 United States of America@43.50637,-70.38023\r\nHotel Phone: 1-207-934-4874\r\n\r\nView the full details of your booking at https://wwwexpediacom.integration.sb.karmalab.net/trips/11616506271\r\n\r\nMap: http://maps.google.com?q=04064+Old+Orchard+Beach,+2+Seacliff+Ave+",
  :end 1468422000,
  :event-type :hotel-checkin,
  :itin-num 11616506271,
  :location "2 Seacliff Ave, Old Orchard Beach, ME, 04064 United States of America@43.50637,-70.38023",
  :start 1468090800,
  :title "Sea Cliff House And Motel, Old Orchard Beach",
  :url "https://wwwexpediacom.integration.sb.karmalab.net/trips/11616506271"}
 {:details "Check Out time\r\nHotel Name: Sea Cliff House And Motel\r\nHotel Address:  2 Seacliff Ave, Old Orchard Beach, ME, 04064 United States of America@43.50637,-70.38023\r\nHotel Phone: 1-207-934-4874\r\n\r\nView the full details of your booking at https://wwwexpediacom.integration.sb.karmalab.net/trips/11616506271\r\n\r\nMap: http://maps.google.com?q=04064+Old+Orchard+Beach,+2+Seacliff+Ave+",
  :end 1468422000,
  :event-type :hotel-checkout,
  :itin-num 11616506271,
  :location "2 Seacliff Ave, Old Orchard Beach, ME, 04064 United States of America@43.50637,-70.38023",
  :start 1468090800,
  :title "Sea Cliff House And Motel, Old Orchard Beach",
  :url "https://wwwexpediacom.integration.sb.karmalab.net/trips/11616506271"}))

(def standalone-flight-events '({:details "Airline:  Delta\r\nFlight Number: 1210\r\nSan Francisco Intl. (SFO) to Hartsfield-Jackson Atlanta Intl. (ATL)\r\nPassengers: Elmer Fudd\r\n\r\nView the full details of your booking at https://wwwexpediacom.integration.sb.karmalab.net/trips/11617273915\r\n\r\n",
  :end 1470593040,
  :event-type :flight,
  :itin-num 11617273915,
  :location "San Francisco Intl. (SFO)@37.61594,-122.387996",
  :start 1470575400,
  :title "Flight: Delta 1210 From San Francisco Intl. To Hartsfield-Jackson Atlanta Intl.",
  :url "https://wwwexpediacom.integration.sb.karmalab.net/trips/11617273915"}
 {:details "Airline:  Delta\r\nFlight Number: 1998\r\nHartsfield-Jackson Atlanta Intl. (ATL) to Miami Intl. (MIA)\r\nPassengers: Elmer Fudd\r\n\r\nView the full details of your booking at https://wwwexpediacom.integration.sb.karmalab.net/trips/11617273915\r\n\r\n",
  :end 1470603900,
  :event-type :flight,
  :itin-num 11617273915,
  :location "Hartsfield-Jackson Atlanta Intl. (ATL)@33.640785,-84.446036",
  :start 1470596400,
  :title "Flight: Delta 1998 From Hartsfield-Jackson Atlanta Intl. To Miami Intl.",
  :url "https://wwwexpediacom.integration.sb.karmalab.net/trips/11617273915"}
 {:details "Airline:  Delta\r\nFlight Number: 2173\r\nMiami Intl. (MIA) to Hartsfield-Jackson Atlanta Intl. (ATL)\r\nPassengers: Elmer Fudd\r\n\r\nView the full details of your booking at https://wwwexpediacom.integration.sb.karmalab.net/trips/11617273915\r\n\r\n",
  :end 1470859980,
  :event-type :flight,
  :itin-num 11617273915,
  :location "Miami Intl. (MIA)@25.79509,-80.27843",
  :start 1470852000,
  :title "Flight: Delta 2173 From Miami Intl. To Hartsfield-Jackson Atlanta Intl.",
  :url "https://wwwexpediacom.integration.sb.karmalab.net/trips/11617273915"}
 {:details "Airline:  Delta\r\nFlight Number: 2145\r\nHartsfield-Jackson Atlanta Intl. (ATL) to San Francisco Intl. (SFO)\r\nPassengers: Elmer Fudd\r\n\r\nView the full details of your booking at https://wwwexpediacom.integration.sb.karmalab.net/trips/11617273915\r\n\r\n",
  :end 1470884160,
  :event-type :flight,
  :itin-num 11617273915,
  :location "Hartsfield-Jackson Atlanta Intl. (ATL)@33.640785,-84.446036",
  :start 1470865500,
  :title "Flight: Delta 2145 From Hartsfield-Jackson Atlanta Intl. To San Francisco Intl.",
  :url "https://wwwexpediacom.integration.sb.karmalab.net/trips/11617273915"}))

(def standalone-activity-events '({:details "Activity Start Time\r\nVendor: Urban Adventures - CAD\r\nSupplier Ref Num: 11616648817\r\nLocation: Narita International Airport : 1-1 Furugome, Narita, Chiba, JPN@35.77199,140.39286\r\n\r\nView the full details of your booking at https://wwwexpediacom.trunk.sb.karmalab.net/trips/7822027698\r\n\r\n",
  :end 1470405600,
  :event-type :activity-start,
  :itin-num 7822027698,
  :location "Narita International Airport : 1-1 Furugome, Narita, Chiba, JPN@35.77199,140.39286",
  :start 1470400200,
  :title "Narita, Japan",
  :url "https://wwwexpediacom.trunk.sb.karmalab.net/trips/7822027698"}
 {:details "Activity End Time\r\nVendor: Urban Adventures - CAD\r\nSupplier Ref Num: 11616648817\r\nLocation: Narita International Airport : 1-1 Furugome, Narita, Chiba, JPN@35.77199,140.39286\r\n\r\nView the full details of your booking at https://wwwexpediacom.trunk.sb.karmalab.net/trips/7822027698\r\n\r\n",
  :end 1470405600,
  :event-type :activity-end,
  :itin-num 7822027698,
  :location "Narita International Airport : 1-1 Furugome, Narita, Chiba, JPN@35.77199,140.39286",
  :start 1470400200,
  :title "Narita, Japan",
  :url "https://wwwexpediacom.trunk.sb.karmalab.net/trips/7822027698"}))

(def flight-hotel-events '({:details "Airline:  Virgin America\r\nFlight Number: 236\r\nLos Angeles Intl. (LAX) to O'Hare Intl. (ORD)\r\nPassengers: Elmer Fudd\r\n\r\nView the full details of your booking at https://wwwexpediacom.integration.sb.karmalab.net/trips/11624965146\r\n\r\n",
  :end 1463199600,
  :event-type :flight,
  :itin-num 11624965146,
  :location "Los Angeles Intl. (LAX)@33.94415,-118.4032",
  :start 1463184900,
  :title "Flight: Virgin America 236 From Los Angeles Intl. To O'Hare Intl.",
  :url "https://wwwexpediacom.integration.sb.karmalab.net/trips/11624965146"}
 {:details "Airline:  Virgin America\r\nFlight Number: 231\r\nO'Hare Intl. (ORD) to Los Angeles Intl. (LAX)\r\nPassengers: Elmer Fudd\r\n\r\nView the full details of your booking at https://wwwexpediacom.integration.sb.karmalab.net/trips/11624965146\r\n\r\n",
  :end 1463424300,
  :event-type :flight,
  :itin-num 11624965146,
  :location "O'Hare Intl. (ORD)@41.976928,-87.904787",
  :start 1463408400,
  :title "Flight: Virgin America 231 From O'Hare Intl. To Los Angeles Intl.",
  :url "https://wwwexpediacom.integration.sb.karmalab.net/trips/11624965146"}
 {:details "Check in Time\r\nHotel Name: Hilton Chicago O'Hare Airport\r\nHotel Address:  Ohare International Airport, Chicago, IL, 60666 United States of America@41.977221,-87.905018\r\nHotel Phone: 1-773-686-8000\r\n\r\nView the full details of your booking at https://wwwexpediacom.integration.sb.karmalab.net/trips/11624965146\r\n\r\nMap: //maps.google.com?q=60666+Chicago,+Ohare+International+Airport+",
  :end 1463418000,
  :event-type :hotel-checkin,
  :itin-num 11624965146,
  :location "Ohare International Airport, Chicago, IL, 60666 United States of America@41.977221,-87.905018",
  :start 1463173200,
  :title "Chicago",
  :url "https://wwwexpediacom.integration.sb.karmalab.net/trips/11624965146"}
 {:details "Check Out time\r\nHotel Name: Hilton Chicago O'Hare Airport\r\nHotel Address:  Ohare International Airport, Chicago, IL, 60666 United States of America@41.977221,-87.905018\r\nHotel Phone: 1-773-686-8000\r\n\r\nView the full details of your booking at https://wwwexpediacom.integration.sb.karmalab.net/trips/11624965146\r\n\r\nMap: //maps.google.com?q=60666+Chicago,+Ohare+International+Airport+",
  :end 1463418000,
  :event-type :hotel-checkout,
  :itin-num 11624965146,
  :location "Ohare International Airport, Chicago, IL, 60666 United States of America@41.977221,-87.905018",
  :start 1463173200,
  :title "Chicago",
  :url "https://wwwexpediacom.integration.sb.karmalab.net/trips/11624965146"}))

(def multi-room-hotel-events '({:details "Check in Time\r\nHotel Name: Grand Hyatt Denver\r\nHotel Address:  1750 Welton St, Denver, CO, 80202 United States of America@39.745869,-104.989537\r\nHotel Phone: 1-303-295-1234\r\n\r\nView the full details of your booking at https://wwwexpediacom.integration.sb.karmalab.net/trips/11624954701\r\n\r\nMap: //maps.google.com?q=80202+Denver,+1750+Welton+St+",
  :end 1460570400,
  :event-type :hotel-checkin,
  :itin-num 11624954701,
  :location "1750 Welton St, Denver, CO, 80202 United States of America@39.745869,-104.989537",
  :start 1460325600,
  :title "Grand Hyatt Denver, Denver",
  :url "https://wwwexpediacom.integration.sb.karmalab.net/trips/11624954701"}
 {:details "Check Out time\r\nHotel Name: Grand Hyatt Denver\r\nHotel Address:  1750 Welton St, Denver, CO, 80202 United States of America@39.745869,-104.989537\r\nHotel Phone: 1-303-295-1234\r\n\r\nView the full details of your booking at https://wwwexpediacom.integration.sb.karmalab.net/trips/11624954701\r\n\r\nMap: //maps.google.com?q=80202+Denver,+1750+Welton+St+",
  :end 1460570400,
  :event-type :hotel-checkout,
  :itin-num 11624954701,
  :location "1750 Welton St, Denver, CO, 80202 United States of America@39.745869,-104.989537",
  :start 1460325600,
  :title "Grand Hyatt Denver, Denver",
  :url "https://wwwexpediacom.integration.sb.karmalab.net/trips/11624954701"}))

(def split-ticket-events '({:details "Airline:  Delta\r\nFlight Number: 5931\r\nLaGuardia (LGA) to O'Hare Intl. (ORD)\r\nPassengers: Elmer Fudd\r\n\r\nView the full details of your booking at https://wwwexpediacom.integration.sb.karmalab.net/trips/11624970813\r\n\r\n",
  :end 1465562700,
  :event-type :flight,
  :itin-num 11624970813,
  :location "LaGuardia (LGA)@40.77429,-73.872035",
  :start 1465553700,
  :title "Flight: Delta 5931 From LaGuardia To O'Hare Intl.",
  :url "https://wwwexpediacom.integration.sb.karmalab.net/trips/11624970813"}
 {:details "Airline:  United\r\nFlight Number: 910\r\nO'Hare Intl. (ORD) to LaGuardia (LGA)\r\nPassengers: Elmer Fudd\r\n\r\nView the full details of your booking at https://wwwexpediacom.integration.sb.karmalab.net/trips/11624970813\r\n\r\n",
  :end 1465747380,
  :event-type :flight,
  :itin-num 11624970813,
  :location "O'Hare Intl. (ORD)@41.976928,-87.904787",
  :start 1465740000,
  :title "Flight: United 910 From O'Hare Intl. To LaGuardia",
  :url "https://wwwexpediacom.integration.sb.karmalab.net/trips/11624970813"}))

; start is in the wrong format in the trips api
(def cruise-events '({:details "Your Cruise: \r\nStart Time:  Oct 2, 2016 4:00 PM\r\nEnd Time: Oct 9, 2016 8:00 AM\r\nCruise Line: Norwegian Cruise Line\r\nShip Name: Norwegian Getaway\r\nConfirmation Number: 29225136\r\n\r\nView the full details of your booking at https://wwwexpedia-aarpcom.trunk.sb.karmalab.net/trips/1062225501?email=aalee@expedia.com\r\n\r\n",
                      :end "Oct 9, 2016 8:00 AM",
                      :event-type :cruise,
                      :location "7-night Cruise from MIAMI",
                      :start "Oct 2, 2016 4:00 PM",
                      :title "7-night Cruise from MIAMI",
                      :url "https://wwwexpedia-aarpcom.trunk.sb.karmalab.net/trips/1062225501?email=aalee@expedia.com"}))

(defn events-for-trip [itin-num]
  (-> itin-num
      build-file-name
      read-trip-from-file
      create-events-for-trip))

(deftest create-events-test
  (testing "events created for trip"
    (is (= standalone-car-events (events-for-trip "7835454236")))
    (is (= standalone-hotel-events (events-for-trip "11616506271")))
    (is (= standalone-flight-events (events-for-trip "11617273915")))
    (is (= standalone-activity-events (events-for-trip "7822027698")))
    (is (= flight-hotel-events (events-for-trip "11624965146")))
    (is (= multi-room-hotel-events (events-for-trip "11624954701")))
    (is (= split-ticket-events (events-for-trip "11624970813")))
    ; cruises is commented out until startdate has proper format
    #_(is (= cruise-events (events-for-trip "7828048848")))))



(def car-calendar #"BEGIN:VCALENDAR\nPRODID:-//Expedia\\, Inc. //Trip Calendar V0.1//EN\nVERSION:2.0\nMETHOD:PUBLISH\nCALSCALE:GREGORIAN\nBEGIN:VEVENT\nDTSTAMP:(\w+)\nDTSTART:20160610T130000Z\nSUMMARY:Car rental in Chicago\nUID:7835454236_0@(\d+)\nORGANIZER:https://wwwexpediacom.integration.sb.karmalab.net/trips/7835454\n 236\nURL:https://wwwexpediacom.integration.sb.karmalab.net/trips/7835454236\nLOCATION:Chicago \(ORD\)\\,  \(Shuttle to counter and car\) OHARE INTL AIRPORT\n \\, Chicago USA@41.979629\\,-87.904565\nDESCRIPTION:Car Pick Up Time.\\nVendor: Alamo\\nCar: Compact TwoDoorCar \\nC\n onfirmation Number: 1203965970COUNT\\nPick Up Instructions:   SEE SHUTTLE\n  POLICY FOR ARRIVAL INFORMATION.\\nDUE TO CONSTRUCTION IN AND AROUND THE \n AIRPORT\\nALLOW EXTRA TIME FOR RENTALS\\n.\\n\*\*\*\*\*\*\*\*\*\*\*\* ALAMO\*S RENTAL CO\n UNTER \*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\\n   AIRPORT - OHARE INTL\\n   COUNTER - CONSOLIDA\n TED ALAMO/NATIONAL COUNTERS\\n   LOCATED THROUGHOUT THE AIRPORT IN LOWER \n LEVEL\\n   BAGGAGE CLAIM AREAS. WHEN RENTAL IS PROCESSED\\n   PROCEED TO A\n LAMO/NATIONAL SHUTTLE BUS.\\n.\\n   WHEN COUNTERS ARE UNMANNED - PROCEED T\n O SHUTTLE.\\n.\\n\*\*\*\*\*\*\*\*\*\*\* ALAMO\*S SHUTTLE SERVICE \*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\\n  \n  SHUTTLE - ARRIVES FROM 11PM-7AM. USE COURTESY\\n   PHONE AT COUNTER TO C\n ALL FOR SHUTTLE BUS.\\n- BOARD ALAMO/NATIONAL SHUTTLE BUS\\n- RUNS EVERY 5\n  MINUTES\\n- INTL TERMINAL ARRIVALS USE COURTESY PHONE AND\\n  CALL 694-46\n 40 FOR ALAMO/NATIONAL SHUTTLE SERVICE.\\n\\nLocation: Chicago \(ORD\)\\,  \(Sh\n uttle to counter and car\) OHARE INTL AIRPORT\\, Chicago USA@41.979629\\,-8\n 7.904565\\n\\nView the full details of your booking at https://wwwexpediac\n om.integration.sb.karmalab.net/trips/7835454236\\n\\n\nEND:VEVENT\nBEGIN:VEVENT\nDTSTAMP:(\w+)\nDTSTART:20160612T120000Z\nSUMMARY:Car rental in Chicago\nUID:7835454236_1@(\d+)\nORGANIZER:https://wwwexpediacom.integration.sb.karmalab.net/trips/7835454\n 236\nURL:https://wwwexpediacom.integration.sb.karmalab.net/trips/7835454236\nLOCATION:Chicago \(ORD\)\\,  \(Shuttle to counter and car\) OHARE INTL AIRPORT\n \\, Chicago USA@41.979629\\,-87.904565\nDESCRIPTION:Car Drop Off Time.\\nVendor: Alamo\\nCar: Compact TwoDoorCar \\n\n Confirmation Number: 1203965970COUNT\\nLocation: Chicago \(ORD\)\\,  \(Shuttl\n e to counter and car\) OHARE INTL AIRPORT\\, Chicago USA@41.979629\\,-87.90\n 4565\\n\\nView the full details of your booking at https://wwwexpediacom.i\n ntegration.sb.karmalab.net/trips/7835454236\\n\\n\nEND:VEVENT\nEND:VCALENDAR\n")

;;  **** in the regex, remember to escape * + ? ()with a backslash \ !!!!!
(def hotel-calendar #"BEGIN:VCALENDAR\nPRODID:-//Expedia\\, Inc. //Trip Calendar V0.1//EN\nVERSION:2.0\nMETHOD:PUBLISH\nCALSCALE:GREGORIAN\nBEGIN:VEVENT\nDTSTAMP:(\w+)\nDTSTART:20160709T190000Z\nSUMMARY:Sea Cliff House And Motel\\, Old Orchard Beach\nUID:11616506271_0@(\d+)\nORGANIZER:https://wwwexpediacom.integration.sb.karmalab.net/trips/1161650\n 6271\nURL:https://wwwexpediacom.integration.sb.karmalab.net/trips/11616506271\nLOCATION:2 Seacliff Ave\\, Old Orchard Beach\\, ME\\, 04064 United States of\n  America@43.50637\\,-70.38023\nDESCRIPTION:Check in Time\\nHotel Name: Sea Cliff House And Motel\\nHotel A\n ddress:  2 Seacliff Ave\\, Old Orchard Beach\\, ME\\, 04064 United States o\n f America@43.50637\\,-70.38023\\nHotel Phone: 1-207-934-4874\\n\\nView the f\n ull details of your booking at https://wwwexpediacom.integration.sb.karm\n alab.net/trips/11616506271\\n\\nMap: http://maps.google.com\?q=04064\+Old\+Or\n chard\+Beach\\,\+2\+Seacliff\+Ave\+\nEND:VEVENT\nBEGIN:VEVENT\nDTSTAMP:(\w+)\nDTSTART:20160713T150000Z\nSUMMARY:Sea Cliff House And Motel\\, Old Orchard Beach\nUID:11616506271_1@(\d+)\nORGANIZER:https://wwwexpediacom.integration.sb.karmalab.net/trips/1161650\n 6271\nURL:https://wwwexpediacom.integration.sb.karmalab.net/trips/11616506271\nLOCATION:2 Seacliff Ave\\, Old Orchard Beach\\, ME\\, 04064 United States of\n  America@43.50637\\,-70.38023\nDESCRIPTION:Check Out time\\nHotel Name: Sea Cliff House And Motel\\nHotel \n Address:  2 Seacliff Ave\\, Old Orchard Beach\\, ME\\, 04064 United States \n of America@43.50637\\,-70.38023\\nHotel Phone: 1-207-934-4874\\n\\nView the \n full details of your booking at https://wwwexpediacom.integration.sb.kar\n malab.net/trips/11616506271\\n\\nMap: http://maps.google.com\?q=04064\+Old\+O\n rchard\+Beach\\,\+2\+Seacliff\+Ave\+\nEND:VEVENT\nEND:VCALENDAR\n")

(def flight-calendar #"BEGIN:VCALENDAR\nPRODID:-//Expedia\\, Inc. //Trip Calendar V0.1//EN\nVERSION:2.0\nMETHOD:PUBLISH\nCALSCALE:GREGORIAN\nBEGIN:VEVENT\nDTSTAMP:(\w+)\nDTSTART:20160807T131000Z\nDTEND:20160807T180400Z\nSUMMARY:Flight: Delta 1210 From San Francisco Intl. To Hartsfield-Jackson\n  Atlanta Intl.\nUID:11617273915_0@(\d+)\nORGANIZER:https://wwwexpediacom.integration.sb.karmalab.net/trips/1161727\n 3915\nURL:https://wwwexpediacom.integration.sb.karmalab.net/trips/11617273915\nLOCATION:San Francisco Intl. \(SFO\)@37.61594\\,-122.387996\nDESCRIPTION:Airline:  Delta\\nFlight Number: 1210\\nSan Francisco Intl. \(SF\n O\) to Hartsfield-Jackson Atlanta Intl. \(ATL\)\\nPassengers: Elmer Fudd\\n\\n\n View the full details of your booking at https://wwwexpediacom.integrati\n on.sb.karmalab.net/trips/11617273915\\n\\n\nEND:VEVENT\nBEGIN:VEVENT\nDTSTAMP:(\w+)\nDTSTART:20160807T190000Z\nDTEND:20160807T210500Z\nSUMMARY:Flight: Delta 1998 From Hartsfield-Jackson Atlanta Intl. To Miami\n  Intl.\nUID:11617273915_1@(\d+)\nORGANIZER:https://wwwexpediacom.integration.sb.karmalab.net/trips/1161727\n 3915\nURL:https://wwwexpediacom.integration.sb.karmalab.net/trips/11617273915\nLOCATION:Hartsfield-Jackson Atlanta Intl. \(ATL\)@33.640785\\,-84.446036\nDESCRIPTION:Airline:  Delta\\nFlight Number: 1998\\nHartsfield-Jackson Atla\n nta Intl. \(ATL\) to Miami Intl. \(MIA\)\\nPassengers: Elmer Fudd\\n\\nView the\n  full details of your booking at https://wwwexpediacom.integration.sb.ka\n rmalab.net/trips/11617273915\\n\\n\nEND:VEVENT\nBEGIN:VEVENT\nDTSTAMP:(\w+)\nDTSTART:20160810T180000Z\nDTEND:20160810T201300Z\nSUMMARY:Flight: Delta 2173 From Miami Intl. To Hartsfield-Jackson Atlanta\n  Intl.\nUID:11617273915_2@(\d+)\nORGANIZER:https://wwwexpediacom.integration.sb.karmalab.net/trips/1161727\n 3915\nURL:https://wwwexpediacom.integration.sb.karmalab.net/trips/11617273915\nLOCATION:Miami Intl. \(MIA\)@25.79509\\,-80.27843\nDESCRIPTION:Airline:  Delta\\nFlight Number: 2173\\nMiami Intl. \(MIA\) to Ha\n rtsfield-Jackson Atlanta Intl. \(ATL\)\\nPassengers: Elmer Fudd\\n\\nView the\n  full details of your booking at https://wwwexpediacom.integration.sb.ka\n rmalab.net/trips/11617273915\\n\\n\nEND:VEVENT\nBEGIN:VEVENT\nDTSTAMP:(\w+)\nDTSTART:20160810T214500Z\nDTEND:20160811T025600Z\nSUMMARY:Flight: Delta 2145 From Hartsfield-Jackson Atlanta Intl. To San F\n rancisco Intl.\nUID:11617273915_3@(\d+)\nORGANIZER:https://wwwexpediacom.integration.sb.karmalab.net/trips/1161727\n 3915\nURL:https://wwwexpediacom.integration.sb.karmalab.net/trips/11617273915\nLOCATION:Hartsfield-Jackson Atlanta Intl. \(ATL\)@33.640785\\,-84.446036\nDESCRIPTION:Airline:  Delta\\nFlight Number: 2145\\nHartsfield-Jackson Atla\n nta Intl. \(ATL\) to San Francisco Intl. \(SFO\)\\nPassengers: Elmer Fudd\\n\\n\n View the full details of your booking at https://wwwexpediacom.integrati\n on.sb.karmalab.net/trips/11617273915\\n\\n\nEND:VEVENT\nEND:VCALENDAR\n")

(def activity-calendar #"BEGIN:VCALENDAR\nPRODID:-//Expedia\\, Inc. //Trip Calendar V0.1//EN\nVERSION:2.0\nMETHOD:PUBLISH\nCALSCALE:GREGORIAN\nBEGIN:VEVENT\nDTSTAMP:(\w+)\nDTSTART:20160805T123000Z\nSUMMARY:Narita\\, Japan\nUID:7822027698_0@(\d+)\nORGANIZER:https://wwwexpediacom.trunk.sb.karmalab.net/trips/7822027698\nURL:https://wwwexpediacom.trunk.sb.karmalab.net/trips/7822027698\nLOCATION:Narita International Airport : 1-1 Furugome\\, Narita\\, Chiba\\, J\n PN@35.77199\\,140.39286\nDESCRIPTION:Activity Start Time\\nVendor: Urban Adventures - CAD\\nSupplier\n  Ref Num: 11616648817\\nLocation: Narita International Airport : 1-1 Furu\n gome\\, Narita\\, Chiba\\, JPN@35.77199\\,140.39286\\n\\nView the full details\n  of your booking at https://wwwexpediacom.trunk.sb.karmalab.net/trips/78\n 22027698\\n\\n\nEND:VEVENT\nBEGIN:VEVENT\nDTSTAMP:(\w+)\nDTSTART:20160805T140000Z\nSUMMARY:Narita\\, Japan\nUID:7822027698_1@(\d+)\nORGANIZER:https://wwwexpediacom.trunk.sb.karmalab.net/trips/7822027698\nURL:https://wwwexpediacom.trunk.sb.karmalab.net/trips/7822027698\nLOCATION:Narita International Airport : 1-1 Furugome\\, Narita\\, Chiba\\, J\n PN@35.77199\\,140.39286\nDESCRIPTION:Activity End Time\\nVendor: Urban Adventures - CAD\\nSupplier R\n ef Num: 11616648817\\nLocation: Narita International Airport : 1-1 Furugo\n me\\, Narita\\, Chiba\\, JPN@35.77199\\,140.39286\\n\\nView the full details o\n f your booking at https://wwwexpediacom.trunk.sb.karmalab.net/trips/7822\n 027698\\n\\n\nEND:VEVENT\nEND:VCALENDAR\n")

(def cruise-calendar #"")

(deftest create-calendar-from-events-test
  (testing "create calendars exercising flights hotels cars activities cruises"
    (is (re-matches car-calendar (calendar-from-events (events-for-trip "7835454236"))))
    (is (re-matches hotel-calendar (calendar-from-events (events-for-trip "11616506271"))))
    (is (re-matches flight-calendar (calendar-from-events (events-for-trip "11617273915"))))
    (is (re-matches activity-calendar (calendar-from-events (events-for-trip "7822027698"))))
    ; cruise will not work until the start date in teh trip JSON api is in the correct format
    #_(is (re-matches cruise-calendar (calendar-from-events (events-for-trip "7828048848"))))
    ))
