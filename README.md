# JOOQ Vernissage

This is the source code for a tech talk I held at LShift about JOOQ (http://jooq.org).

Using off the shelf Maven plugins, it demonstrates:

* Migrating a local database with Flyway;
* Introspecting the resulting schema with JOOQ to generate code artifacts;
* Running a test that compiles against the generated code;
* This test demonstrates some basic API usage to read and write to and from the DB using JOOQ;
* If you add a second DDL migration, that say introduces a breaking change to the DDL, Flyway will migrate this,
JOOQ will re-generate artifacts and then the compiler will complain that the DB access is no longer type safe;
