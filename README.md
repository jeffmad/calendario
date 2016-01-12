# calendario

FIXME: description

## Developing

### Setup

When you first clone this repository, run:

```sh
lein setup
```

This will create files for local configuration, and prep your system
for the project.

### Postgresql

Install postgres
On osx
```sh
brew install postgresql
```

Then, create init caldb
```sh
psql -f db/init_db.sql
```

Run flyway
```sh
./db/flyway-3.2.1/flyway -configFile=db/flyway-3.2.1/conf/flyway.conf migrate
```

### Environment

To begin developing, start with a REPL.

```sh
lein repl
```

Run `go` to initiate and start the system.

```clojure
user=> (go)
:started
```

By default this creates a web server at <http://localhost:3000>.

When you make changes to your source files, use `reset` to reload any
modified files and reset the server.

```clojure
user=> (reset)
:reloading (...)
:resumed
```

### Testing

Testing is fastest through the REPL, as you avoid environment startup
time.

```clojure
user=> (test)
...
```

But you can also run tests through Leiningen.

```sh
lein test
```
a cool way to see if a command is installed.
```
command -v convert >/dev/null 2>&1 || { echo "ImageMagick is a required dependency, aborting..." >&2; exit 1; }
```
### Generators

This project has several [generators][] to help you create files.

* `lein gen endpoint <name>` to create a new endpoint
* `lein gen component <name>` to create a new component

[generators]: https://github.com/weavejester/lein-generate

## Deploying

### One time database setup
The database should only have to be set up once per environment.

Create a postgres RDS instance for the environment using one of the rds-create-instance jenkins jobs.
https://ewe.deploy.sb.karmalab.net:8443/job/ewetest_rds-create-instance/

Run the init_db script on the db.
```sh
psql -h caldb.cmguqnu4wehw.us-west-2.rds.amazonaws.com -U cal -d caldb -f db/init_db.sql
```

Run flyway migrate:
```sh
./db/flyway-3.2.1/flyway -url=jdbc:postgresql://caldb.cmguqnu4wehw.us-west-2.rds.amazonaws.com:5432/caldb -user=caldba -password=333BlackTower migrate
```

### Deploying

FIXME: steps to deploy

## Legal

Copyright © 2015 FIXME
