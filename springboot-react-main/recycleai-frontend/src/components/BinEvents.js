import React, { useMemo } from "react";
import PropTypes from "prop-types";
import dayjs from "dayjs";
import { useNavigate } from "react-router-dom";

const BinEvents = ({ logs, alerts, binId }) => {
  const navigate = useNavigate();

  const events = useMemo(() => {
    const combined = [
      ...logs.map((l) => ({ ...l, type: "log", id: l.logId })),
      ...alerts.map((a) => ({ ...a, type: "alert", id: a.alertId })),
    ];
    
    return combined
      .filter((item) => item.datetime)
      .sort((a, b) => new Date(b.datetime) - new Date(a.datetime))
      .slice(0, 10);
  }, [logs, alerts]);

  return (
    <div className="right-column">
      <button
        className="report-btn"
        onClick={() => navigate(`/bin/${binId}/report`)}
      >
        Report Issue
      </button>
      <h2>Events</h2>
      <div className="logs-list">
        {events.map((item) => (
          <div
            key={`${item.type}-${item.id}`}
            className={`log-item ${item.type === "log" ? "log-green" : "log-red"}`}
          >
            <p><strong>{item.description}</strong></p>
            <p>{dayjs(item.datetime).format("DD/MM/YYYY HH:mm:ss")}</p>
          </div>
        ))}
        {events.length === 0 && <p style={{textAlign: "center"}}>No recent events</p>}
      </div>
    </div>
  );
};

BinEvents.propTypes = {
    logs: PropTypes.arrayOf(PropTypes.shape({
        logId: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
        datetime: PropTypes.string,
        description: PropTypes.string,
    })).isRequired,
    alerts: PropTypes.arrayOf(PropTypes.shape({
        alertId: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
        datetime: PropTypes.string,
        description: PropTypes.string,
    })).isRequired,
    binId: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
};

export default BinEvents;