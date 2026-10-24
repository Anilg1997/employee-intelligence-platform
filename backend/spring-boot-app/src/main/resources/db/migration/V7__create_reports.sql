create table reports (
    id bigserial primary key,
    tenant_id varchar(255) not null,
    report_type varchar(32) not null,
    employee_id bigint,
    department varchar(255),
    status varchar(32) not null,
    content text not null,
    ai_assisted boolean not null default false,
    limitations text not null,
    requester varchar(255) not null,
    human_review_state varchar(32) not null,
    created_at timestamp with time zone not null,
    constraint reports_type_check check (report_type in ('EMPLOYEE','DEPARTMENT','WORKFORCE')),
    constraint reports_status_check check (status in ('GENERATED','FAILED')),
    constraint reports_review_check check (human_review_state in ('PENDING','REVIEWED','REJECTED')),
    constraint reports_target_check check ((report_type = 'EMPLOYEE' and employee_id is not null and department is null)
        or (report_type = 'DEPARTMENT' and department is not null and employee_id is null)
        or (report_type = 'WORKFORCE' and employee_id is null and department is null))
);
create index reports_tenant_created_idx on reports (tenant_id, created_at desc);
create index reports_tenant_id_idx on reports (tenant_id, id);
