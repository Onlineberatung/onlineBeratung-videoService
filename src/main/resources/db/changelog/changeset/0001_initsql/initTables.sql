-- create sql table containing roomId, started at, closed at, and videlink columns

CREATE TABLE videoservice.`videoroom` (
  `id` bigint(21) NOT NULL,
  `session_id` bigint(21) NULL,
  `group_chat_id` bigint(21) NULL,
  `jitsi_room_id` bigint(21) NOT NULL,
  `video_link` varchar(1024) COLLATE utf8_unicode_ci NOT NULL,
  `create_date` datetime NOT NULL DEFAULT (UTC_TIMESTAMP),
  `close_date` datetime NOT NULL DEFAULT (UTC_TIMESTAMP),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


ALTER TABLE videoservice.`videoroom`
ADD CONSTRAINT unique_jitsi_room_id UNIQUE (jitsi_room_id);

CREATE SEQUENCE videoservice.sequence_videoroom
INCREMENT BY 1
MINVALUE = 0
NOMAXVALUE
START WITH 0
CACHE 0;
