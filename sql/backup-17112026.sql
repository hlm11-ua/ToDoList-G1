--
-- PostgreSQL database dump
--

\restrict WaWlvNlZULYxOhizCMyNrw5kK4Ax9SUFRrTyYU0siRzusudhEDckcDNdHTJskaC

-- Dumped from database version 13.23 (Debian 13.23-1.pgdg13+1)
-- Dumped by pg_dump version 13.23 (Debian 13.23-1.pgdg13+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: equipo_usuario; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.equipo_usuario (
    fk_equipo bigint NOT NULL,
    fk_usuario bigint NOT NULL
);


--
-- Name: equipos; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.equipos (
    id bigint NOT NULL,
    nombre character varying(255) NOT NULL
);


--
-- Name: equipos_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.equipos_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: equipos_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.equipos_id_seq OWNED BY public.equipos.id;


--
-- Name: tableros; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tableros (
    id bigint NOT NULL,
    nombre character varying(255) NOT NULL,
    equipo_id bigint NOT NULL
);


--
-- Name: tableros_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tableros_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tableros_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tableros_id_seq OWNED BY public.tableros.id;


--
-- Name: tareas; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tareas (
    id bigint NOT NULL,
    titulo character varying(255) NOT NULL,
    usuario_id bigint NOT NULL,
    estado character varying(255) NOT NULL,
    tablero_id bigint,
    usuario_asignado_id bigint
);


--
-- Name: tareas_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tareas_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tareas_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tareas_id_seq OWNED BY public.tareas.id;


--
-- Name: usuarios; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.usuarios (
    id bigint NOT NULL,
    admin boolean NOT NULL,
    bloqueado boolean NOT NULL,
    email character varying(255) NOT NULL,
    fecha_nacimiento date,
    nombre character varying(255),
    password character varying(255)
);


--
-- Name: usuarios_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.usuarios_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: usuarios_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.usuarios_id_seq OWNED BY public.usuarios.id;


--
-- Name: equipos id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.equipos ALTER COLUMN id SET DEFAULT nextval('public.equipos_id_seq'::regclass);


--
-- Name: tableros id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tableros ALTER COLUMN id SET DEFAULT nextval('public.tableros_id_seq'::regclass);


--
-- Name: tareas id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tareas ALTER COLUMN id SET DEFAULT nextval('public.tareas_id_seq'::regclass);


--
-- Name: usuarios id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuarios ALTER COLUMN id SET DEFAULT nextval('public.usuarios_id_seq'::regclass);


--
-- Data for Name: equipo_usuario; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.equipo_usuario (fk_equipo, fk_usuario) FROM stdin;
1	1
2	2
2	3
2	4
2	5
\.


--
-- Data for Name: equipos; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.equipos (id, nombre) FROM stdin;
1	Equipo Legacy 1.2.0
2	ToDoList-G01
\.


--
-- Data for Name: tableros; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.tableros (id, nombre, equipo_id) FROM stdin;
1	TABLERO MADS	1
2	TABLERO ToDoList-G01	2
\.


--
-- Data for Name: tareas; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.tareas (id, titulo, usuario_id, estado, tablero_id, usuario_asignado_id) FROM stdin;
1	Tarea heredada de la 1.2.0	1	PENDIENTE	\N	\N
4	Crear tablero	3	COMPLETADA	2	3
5	Información perfil	4	EN_PROGRESO	2	4
3	Administrar Tableros	2	PENDIENTE	2	2
6	Estado tareas	5	COMPLETADA	2	5
7	Hacer parte 2 de MADS	2	EN_PROGRESO	\N	\N
\.


--
-- Data for Name: usuarios; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.usuarios (id, admin, bloqueado, email, fecha_nacimiento, nombre, password) FROM stdin;
1	t	f	admin@ua.es	\N	Admin Antiguo	1234
2	f	f	helena@gmail.com	1969-07-23	Helena	1234
3	f	f	ariana@gmail.com	1969-07-23	Ariana	1234
4	f	f	adan@gmail.com	2005-07-21	Adán	1234
5	f	f	crystal@gmail.com	1969-07-23	Crystal	1234
\.


--
-- Name: equipos_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.equipos_id_seq', 2, true);


--
-- Name: tableros_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.tableros_id_seq', 2, true);


--
-- Name: tareas_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.tareas_id_seq', 7, true);


--
-- Name: usuarios_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.usuarios_id_seq', 5, true);


--
-- Name: equipo_usuario equipo_usuario_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.equipo_usuario
    ADD CONSTRAINT equipo_usuario_pkey PRIMARY KEY (fk_equipo, fk_usuario);


--
-- Name: equipos equipos_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.equipos
    ADD CONSTRAINT equipos_pkey PRIMARY KEY (id);


--
-- Name: tableros tableros_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tableros
    ADD CONSTRAINT tableros_pkey PRIMARY KEY (id);


--
-- Name: tareas tareas_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tareas
    ADD CONSTRAINT tareas_pkey PRIMARY KEY (id);


--
-- Name: usuarios usuarios_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_pkey PRIMARY KEY (id);


--
-- Name: tareas fkdmoaxl7yv4q6vkc9h32wvbddr; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tareas
    ADD CONSTRAINT fkdmoaxl7yv4q6vkc9h32wvbddr FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id);


--
-- Name: equipo_usuario fkk4y9gec15ccnirp3r7w29o66p; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.equipo_usuario
    ADD CONSTRAINT fkk4y9gec15ccnirp3r7w29o66p FOREIGN KEY (fk_equipo) REFERENCES public.equipos(id);


--
-- Name: tareas fkkkpjcjdm2oh3wek5ec5f5rwss; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tareas
    ADD CONSTRAINT fkkkpjcjdm2oh3wek5ec5f5rwss FOREIGN KEY (usuario_asignado_id) REFERENCES public.usuarios(id);


--
-- Name: tableros fkn23fdvtkwsddbdh2gij50iai5; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tableros
    ADD CONSTRAINT fkn23fdvtkwsddbdh2gij50iai5 FOREIGN KEY (equipo_id) REFERENCES public.equipos(id);


--
-- Name: equipo_usuario fksabsvjgvfuen6hmyg7gn7oq4v; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.equipo_usuario
    ADD CONSTRAINT fksabsvjgvfuen6hmyg7gn7oq4v FOREIGN KEY (fk_usuario) REFERENCES public.usuarios(id);


--
-- Name: tareas fksqj5mtew63jjlb55n4yfxqq0; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tareas
    ADD CONSTRAINT fksqj5mtew63jjlb55n4yfxqq0 FOREIGN KEY (tablero_id) REFERENCES public.tableros(id);


--
-- PostgreSQL database dump complete
--

\unrestrict WaWlvNlZULYxOhizCMyNrw5kK4Ax9SUFRrTyYU0siRzusudhEDckcDNdHTJskaC

