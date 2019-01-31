CREATE TABLE entry
(
  id integer NOT NULL,
  content character varying(1024) NOT NULL,
  creationdate timestamp without time zone NOT NULL,
  CONSTRAINT entry_pkey PRIMARY KEY (id)
)