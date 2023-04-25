CREATE TABLE IF NOT EXISTS VIDEOROOM
(
    ID bigint NOT NULL,
    jitsi_room_id bigint NOT NULL,
    videolink varchar(2048) NOT NULL,
    create_date datetime NOT NULL,
    close_date datetime,
    CONSTRAINT PK_VIDEOROOM PRIMARY KEY (ID)
);

ALTER TABLE VIDEOROOM
ADD CONSTRAINT IF NOT EXISTS unique_jitsi_room_id UNIQUE (jitsi_room_id);

CREATE SEQUENCE IF NOT EXISTS SEQUENCE_VIDEOROOM
    START WITH 100000
    INCREMENT BY 1;



