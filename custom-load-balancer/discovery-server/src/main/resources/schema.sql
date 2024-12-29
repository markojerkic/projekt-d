drop view if exists best_instance;

create
or replace view best_instance as
select
  s.entry_id,
  s.service_model_id as service_id,
  max(s.instance_recorded_at) as latest_timestamp
from
  service_instance s
where
  s.is_healthy is true
  and strftime ('%s', 'now') * 1000 - s.instance_recorded_at <= 3 * 60 * 1000
group by
  s.instance_id;
