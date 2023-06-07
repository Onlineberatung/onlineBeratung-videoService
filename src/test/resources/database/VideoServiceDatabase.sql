CREATE TABLE IF NOT EXISTS VIDEOROOM
(
    ID bigint NOT NULL,
    session_id bigint NULL,
    group_chat_id bigint NULL,
    jitsi_room_id varchar(40) NOT NULL,
    rocketchat_room_id varchar(40) NOT NULL,
    create_date datetime NOT NULL,
    close_date datetime,
    CONSTRAINT PK_VIDEOROOM PRIMARY KEY (ID)
);

ALTER TABLE VIDEOROOM
ADD CONSTRAINT IF NOT EXISTS unique_jitsi_room_id UNIQUE (jitsi_room_id);

CREATE SEQUENCE IF NOT EXISTS SEQUENCE_VIDEOROOM
    START WITH 100000
    INCREMENT BY 1;



