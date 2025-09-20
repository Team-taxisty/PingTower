-- Минимальная инициализация БД для PingTower
-- Spring Boot создаст таблицы автоматически через JPA

-- Quartz scheduler tables for PostgreSQL
CREATE TABLE qrtz_job_details
(
    sched_name text NOT NULL,
    job_name  text NOT NULL,
    job_group text NOT NULL,
    description text NULL,
    job_class_name   text NOT NULL,
    is_durable bool NOT NULL,
    is_nonconcurrent bool NOT NULL,
    is_update_data bool NOT NULL,
    requests_recovery bool NOT NULL,
    job_data bytea NULL,
    PRIMARY KEY (sched_name,job_name,job_group)
);

CREATE TABLE qrtz_triggers
(
    sched_name text NOT NULL,
    trigger_name text NOT NULL,
    trigger_group text NOT NULL,
    job_name  text NOT NULL,
    job_group text NOT NULL,
    description text NULL,
    next_fire_time bigint NULL,
    prev_fire_time bigint NULL,
    priority integer NULL,
    trigger_state text NOT NULL,
    trigger_type text NOT NULL,
    start_time bigint NOT NULL,
    end_time bigint NULL,
    calendar_name text NULL,
    misfire_instr smallint NULL,
    job_data bytea NULL,
    PRIMARY KEY (sched_name,trigger_name,trigger_group),
    FOREIGN KEY (sched_name,job_name,job_group)
	REFERENCES qrtz_job_details(sched_name,job_name,job_group)
);

CREATE TABLE qrtz_simple_triggers
(
    sched_name text NOT NULL,
    trigger_name text NOT NULL,
    trigger_group text NOT NULL,
    repeat_count bigint NOT NULL,
    repeat_interval bigint NOT NULL,
    times_triggered bigint NOT NULL,
    PRIMARY KEY (sched_name,trigger_name,trigger_group),
    FOREIGN KEY (sched_name,trigger_name,trigger_group)
	REFERENCES qrtz_triggers(sched_name,trigger_name,trigger_group)
);

CREATE TABLE qrtz_cron_triggers
(
    sched_name text NOT NULL,
    trigger_name text NOT NULL,
    trigger_group text NOT NULL,
    cron_expression text NOT NULL,
    time_zone_id text,
    PRIMARY KEY (sched_name,trigger_name,trigger_group),
    FOREIGN KEY (sched_name,trigger_name,trigger_group)
	REFERENCES qrtz_triggers(sched_name,trigger_name,trigger_group)
);

CREATE TABLE qrtz_simprop_triggers
(
    sched_name text NOT NULL,
    trigger_name text NOT NULL,
    trigger_group text NOT NULL,
    str_prop_1 text NULL,
    str_prop_2 text NULL,
    str_prop_3 text NULL,
    int_prop_1 int NULL,
    int_prop_2 int NULL,
    long_prop_1 bigint NULL,
    long_prop_2 bigint NULL,
    dec_prop_1 numeric(13,4) NULL,
    dec_prop_2 numeric(13,4) NULL,
    bool_prop_1 bool NULL,
    bool_prop_2 bool NULL,
    PRIMARY KEY (sched_name,trigger_name,trigger_group),
    FOREIGN KEY (sched_name,trigger_name,trigger_group)
	REFERENCES qrtz_triggers(sched_name,trigger_name,trigger_group)
);

CREATE TABLE qrtz_blob_triggers
(
    sched_name text NOT NULL,
    trigger_name text NOT NULL,
    trigger_group text NOT NULL,
    blob_data bytea NULL,
    PRIMARY KEY (sched_name,trigger_name,trigger_group),
    FOREIGN KEY (sched_name,trigger_name,trigger_group)
	REFERENCES qrtz_triggers(sched_name,trigger_name,trigger_group)
);

CREATE TABLE qrtz_calendars
(
    sched_name text NOT NULL,
    calendar_name  text NOT NULL,
    calendar bytea NOT NULL,
    PRIMARY KEY (sched_name,calendar_name)
);

CREATE TABLE qrtz_paused_trigger_grps
(
    sched_name text NOT NULL,
    trigger_group  text NOT NULL,
    PRIMARY KEY (sched_name,trigger_group)
);

CREATE TABLE qrtz_fired_triggers
(
    sched_name text NOT NULL,
    entry_id text NOT NULL,
    trigger_name text NOT NULL,
    trigger_group text NOT NULL,
    instance_name text NOT NULL,
    fired_time bigint NOT NULL,
    sched_time bigint NOT NULL,
    priority integer NOT NULL,
    state text NOT NULL,
    job_name text NOT NULL,
    job_group text NOT NULL,
    is_nonconcurrent bool NULL,
    requests_recovery bool NULL,
    PRIMARY KEY (sched_name,entry_id)
);

CREATE TABLE qrtz_scheduler_state
(
    sched_name text NOT NULL,
    instance_name text NOT NULL,
    last_checkin_time bigint NOT NULL,
    checkin_interval bigint NOT NULL,
    PRIMARY KEY (sched_name,instance_name)
);

CREATE TABLE qrtz_locks
(
    sched_name text NOT NULL,
    lock_name  text NOT NULL,
    PRIMARY KEY (sched_name,lock_name)
);

create index idx_qrtz_j_req_recovery on qrtz_job_details(sched_name,requests_recovery);
create index idx_qrtz_j_grp on qrtz_job_details(sched_name,job_group);

create index idx_qrtz_t_j on qrtz_triggers(sched_name,job_name,job_group);
create index idx_qrtz_t_jg on qrtz_triggers(sched_name,job_group);
create index idx_qrtz_t_c on qrtz_triggers(sched_name,calendar_name);
create index idx_qrtz_t_g on qrtz_triggers(sched_name,trigger_group);
create index idx_qrtz_t_state on qrtz_triggers(sched_name,trigger_state);
create index idx_qrtz_t_n_state on qrtz_triggers(sched_name,trigger_name,trigger_group,trigger_state);
create index idx_qrtz_t_n_g_state on qrtz_triggers(sched_name,trigger_group,trigger_state);
create index idx_qrtz_t_next_fire_time on qrtz_triggers(sched_name,next_fire_time);
create index idx_qrtz_t_nft_st on qrtz_triggers(sched_name,trigger_state,next_fire_time);
create index idx_qrtz_t_nft_misfire on qrtz_triggers(sched_name,misfire_instr,next_fire_time);
create index idx_qrtz_t_nft_st_misfire on qrtz_triggers(sched_name,misfire_instr,next_fire_time,trigger_state);
create index idx_qrtz_t_nft_st_misfire_grp on qrtz_triggers(sched_name,misfire_instr,next_fire_time,trigger_group,trigger_state);

create index idx_qrtz_ft_trig_inst_name on qrtz_fired_triggers(sched_name,instance_name);
create index idx_qrtz_ft_inst_job_req_rcvry on qrtz_fired_triggers(sched_name,instance_name,requests_recovery);
create index idx_qrtz_ft_j_g on qrtz_fired_triggers(sched_name,job_name,job_group);
create index idx_qrtz_ft_jg on qrtz_fired_triggers(sched_name,job_group);
create index idx_qrtz_ft_t_g on qrtz_fired_triggers(sched_name,trigger_name,trigger_group);
create index idx_qrtz_ft_tg on qrtz_fired_triggers(sched_name,trigger_group);
