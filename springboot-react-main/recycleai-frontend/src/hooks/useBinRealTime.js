import { useState, useEffect } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

const addUnique = (newItem, idKey) => (prev) => {
    const index = prev.findIndex(t => t[idKey] === newItem[idKey]);
    if (index === -1) return [newItem, ...prev];
    return prev;
};

const useBinRealTime = (binId, initialLogs = [], initialImage = "") => {
  const [currentImage, setCurrentImage] = useState(initialImage);
  const [logs, setLogs] = useState(initialLogs);
  const [alerts, setAlerts] = useState([]);

  useEffect(() => {
    const fetchData = async () => {
        try {
            const res = await fetch(`http://localhost:8080/api/alerts/${binId}`);
            const data = await res.json();
            setAlerts(data);
        } catch (err) {
            console.error("Error fetching alerts:", err);
        }

        if (!initialImage) {
            try {
                const res = await fetch(`http://localhost:8080/api/images/latest/${binId}`);
                if (res.ok) {
                    const data = await res.json();
                    setCurrentImage(`http://localhost:8080${data.path}`);
                } else {
                    throw new Error("No image found");
                }
            } catch (error) {
                console.warn("Failed to load initial image", error);
                setCurrentImage("");
            }
        }
    };
    fetchData();
  }, [binId, initialImage]);

  useEffect(() => {
    const handleLog = (msg) => {
        const item = JSON.parse(msg.body);
        if (item.logId) {
          setLogs(addUnique(item, 'logId'));
        }
    };

    const handleAlert = (msg) => {
        const item = JSON.parse(msg.body);
        if (item.alertId) {
          setAlerts(addUnique(item, 'alertId'));
        }
    };

    const handleImage = (msg) => {
        const data = JSON.parse(msg.body);
        setCurrentImage(`http://localhost:8080${data.path}`);
    };

    const socket = new SockJS("http://localhost:8080/ws");
    const client = new Client({
      webSocketFactory: () => socket,
      onConnect: () => {
        client.subscribe(`/topic/logs/${binId}`, handleLog);
        client.subscribe(`/topic/alerts/${binId}`, handleAlert);
        client.subscribe(`/topic/images/${binId}`, handleImage);
      },
    });

    client.activate();
    return () => client.deactivate();
  }, [binId]);

  return { currentImage, logs, alerts };
};

export default useBinRealTime;