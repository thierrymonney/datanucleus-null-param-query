datanucleus-null-param-query
============================

Simple test case for a supposed Datanucleus issue. See http://www.datanucleus.org/servlet/forum/viewthread_thread,7579

To showcase the issue, simply run ```mvn test```

The failing test parses this JDOQL query:
```sql
SELECT FROM ch.thierry.datanucleus.Person WHERE pName == null || name == pName PARAMETERS java.lang.String pName
```

and generates the following (formatted) SQL for HSQLDB:
```sql
SELECT 
    'ch.thierry.datanucleus.Person' AS NUCLEUS_TYPE,
    A0.ID,
    A0.NAME,
    A0.PERSON_ID
FROM
    PERSON A0
WHERE
    (? = NULL) OR (A0.NAME = ?)
```

The passing test directly uses JDBC with the following SQL:
```sql
SELECT 
    'ch.thierry.datanucleus.Person' AS NUCLEUS_TYPE,
    A0.ID,
    A0.NAME,
    A0.PERSON_ID
FROM
    PERSON A0
WHERE
    (? IS NULL) OR (A0.NAME = ?)
```

(The difference is ```(? = NULL)``` vs ```(? IS NULL)```
