-- Script de migración de la base de datos de la versión 1.2.0 a la 1.3.0

-- 1. Crear la tabla tableros y su secuencia
CREATE TABLE public.tableros (
                                 id bigint NOT NULL,
                                 nombre character varying(255) NOT NULL,
                                 equipo_id bigint NOT NULL
);

ALTER TABLE public.tableros OWNER TO mads;

CREATE SEQUENCE public.tableros_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE public.tableros_id_seq OWNER TO mads;
ALTER SEQUENCE public.tableros_id_seq OWNED BY public.tableros.id;
ALTER TABLE ONLY public.tableros ALTER COLUMN id SET DEFAULT nextval('public.tableros_id_seq'::regclass);

ALTER TABLE ONLY public.tableros
    ADD CONSTRAINT tableros_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.tableros
    ADD CONSTRAINT fkn23fdvtkwsddbdh2gij50iai5 FOREIGN KEY (equipo_id) REFERENCES public.equipos(id);

-- 2. Añadir las nuevas columnas a la tabla tareas
-- Se establece DEFAULT 'PENDIENTE' para rellenar las tareas existentes de la v1.2.0 sin violar NOT NULL
ALTER TABLE ONLY public.tareas
    ADD COLUMN estado character varying(255) NOT NULL DEFAULT 'PENDIENTE';

-- Eliminamos el DEFAULT tras poblar las filas existentes para que coincida exactamente con schema-1.3.0.sql
ALTER TABLE ONLY public.tareas
ALTER COLUMN estado DROP DEFAULT;

ALTER TABLE ONLY public.tareas
    ADD COLUMN tablero_id bigint,
    ADD COLUMN usuario_asignado_id bigint;

-- 3. Añadir las claves foráneas de tareas hacia tableros y usuarios
ALTER TABLE ONLY public.tareas
    ADD CONSTRAINT fksqj5mtew63jjlb55n4yfxqq0 FOREIGN KEY (tablero_id) REFERENCES public.tableros(id);

ALTER TABLE ONLY public.tareas
    ADD CONSTRAINT fkkkpjcjdm2oh3wek5ec5f5rwss FOREIGN KEY (usuario_asignado_id) REFERENCES public.usuarios(id);