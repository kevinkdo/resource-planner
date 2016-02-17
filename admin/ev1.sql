

DROP TABLE IF EXISTS reservations;
DROP TABLE IF EXISTS resourcetags;
DROP TABLE IF EXISTS resources;
DROP TABLE IF EXISTS users;

--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: reservations; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE reservations (
    reservation_id integer NOT NULL,
    user_id integer NOT NULL,
    resource_id integer NOT NULL,
    begin_time timestamp without time zone,
    end_time timestamp without time zone,
    should_email boolean NOT NULL
);


ALTER TABLE public.reservations OWNER TO postgres;

--
-- Name: reservations_reservation_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE reservations_reservation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.reservations_reservation_id_seq OWNER TO postgres;

--
-- Name: reservations_reservation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE reservations_reservation_id_seq OWNED BY reservations.reservation_id;


--
-- Name: resources; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE resources (
    resource_id integer NOT NULL,
    name character varying(255) NOT NULL,
    description character varying(2000)
);


ALTER TABLE public.resources OWNER TO postgres;

--
-- Name: resources_resource_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE resources_resource_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.resources_resource_id_seq OWNER TO postgres;

--
-- Name: resources_resource_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE resources_resource_id_seq OWNED BY resources.resource_id;


--
-- Name: resourcetags; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE resourcetags (
    resource_id integer NOT NULL,
    tag character varying(255) NOT NULL
);


ALTER TABLE public.resourcetags OWNER TO postgres;

--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE users (
    user_id integer NOT NULL,
    email character varying(255) NOT NULL,
    passhash character varying(255) NOT NULL,
    username character varying(255) NOT NULL,
    permission integer NOT NULL,
    should_email boolean NOT NULL
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Name: users_user_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE users_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.users_user_id_seq OWNER TO postgres;

--
-- Name: users_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE users_user_id_seq OWNED BY users.user_id;


--
-- Name: reservation_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY reservations ALTER COLUMN reservation_id SET DEFAULT nextval('reservations_reservation_id_seq'::regclass);


--
-- Name: resource_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY resources ALTER COLUMN resource_id SET DEFAULT nextval('resources_resource_id_seq'::regclass);


--
-- Name: user_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY users ALTER COLUMN user_id SET DEFAULT nextval('users_user_id_seq'::regclass);


--
-- Data for Name: reservations; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY reservations (reservation_id, user_id, resource_id, begin_time, end_time, should_email) FROM stdin;
1	2	1	2011-08-06 06:54:00	2012-08-06 07:00:00	t
2	2	1	2015-08-06 01:54:00	2017-08-06 07:54:00	f
3	2	3	2016-08-06 02:54:00	2018-08-06 07:34:00	f
4	2	4	2016-08-06 03:54:00	2016-08-06 07:44:00	f
5	1	5	2016-08-05 22:54:00	2016-08-06 07:54:00	t
\.


--
-- Name: reservations_reservation_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('reservations_reservation_id_seq', 5, true);


--
-- Data for Name: resources; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY resources (resource_id, name, description) FROM stdin;
1	Brown Cat	A pretty brown cat
2	White Dog	My dog
3	Black Kitty	Meow, said the kat
4	Kat Toy	Mhmm
5	Dog Toy	Woof-woof
\.


--
-- Name: resources_resource_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('resources_resource_id_seq', 5, true);


--
-- Data for Name: resourcetags; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY resourcetags (resource_id, tag) FROM stdin;
1	animal
1	cute
2	animal
2	ferocious
3	animal
3	cute
3	baby
5	expensive
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY users (user_id, email, passhash, username, permission, should_email) FROM stdin;
1	admin@admin.com	1000:9816dd56235c68a566b1f50a1815ab96761ebf7ad33d84cd:5b209a5f9b1628fbd80cdffb0aa50b7ec58f07e93f9b18fc	admin	1	f
2	jiaweizhang95@gmail.com	1000:9b33e34e08226378ececa3cc25c912ded5ed45419fb6c83d:67bfddb9cf214c85ce13ee7baf6c907d96ca0113a1bf2746	jiaweizhang95	0	t
3	randomguy@gmail.com	1000:f02b71360bca4fc605085d8d0f3da4c5f71e112f21282179:9e7b894597f44f8a90bcda83361337168cd0860419ac2469	randomguy	0	f
\.


--
-- Name: users_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('users_user_id_seq', 3, true);


--
-- Name: reservations_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY reservations
    ADD CONSTRAINT reservations_pkey PRIMARY KEY (reservation_id);


--
-- Name: resources_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY resources
    ADD CONSTRAINT resources_pkey PRIMARY KEY (resource_id);


--
-- Name: users_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- Name: users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);


--
-- Name: users_username_key; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_username_key UNIQUE (username);


--
-- Name: reservations_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY reservations
    ADD CONSTRAINT reservations_resource_id_fkey FOREIGN KEY (resource_id) REFERENCES resources(resource_id) ON DELETE CASCADE;


--
-- Name: reservations_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY reservations
    ADD CONSTRAINT reservations_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;


--
-- Name: resourcetags_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY resourcetags
    ADD CONSTRAINT resourcetags_resource_id_fkey FOREIGN KEY (resource_id) REFERENCES resources(resource_id) ON DELETE CASCADE;


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

