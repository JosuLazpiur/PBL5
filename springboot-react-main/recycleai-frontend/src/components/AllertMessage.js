import React from "react";
import PropTypes from "prop-types";

const AlertMessage = ({ message }) => {
    if (!message) return null;

    return (
        <div className="alert alert-danger mt-3" role="alert">
            {message}
        </div>
    );
};

AlertMessage.propTypes = {
    message: PropTypes.string,
};

export default AlertMessage;