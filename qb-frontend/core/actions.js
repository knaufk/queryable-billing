/*
 * action types
 */

export const CHANGE_SUBSCRIBER = "CHANGE_SUBSCRIBER"
export const FETCH_SUBTOTAL = "FETCH_SUBTOTAL"
export const RECEIVE_SUBTOTAL = "RECEIVE_SUBTOTAL"
export const FAILED_RECEIVE_SUBTOTAL = "FAILED_RECEIVE_SUBTOTAL"

import fetch from 'isomorphic-fetch'

/*
 * action creators
 */

export function changeSubscriber(subscriber) {
    return { type: CHANGE_SUBSCRIBER, subscriber: subscriber}
}

export function requestSubtotal(subscriber) {
    return {type: FETCH_SUBTOTAL, subscriber: subscriber}
}


function getTimeParsed() {
    var currentDate = new Date();

    function padWithZero(toPad) {
        return toPad <= 9 ? '0' + toPad : toPad;
    }

    var time = currentDate.getDate() + "/"
        + (currentDate.getMonth() + 1) + "/"
        + currentDate.getFullYear() + " @ "
        + padWithZero(currentDate.getHours()) + ":"
        + padWithZero(currentDate.getMinutes()) + ":"
        + padWithZero(currentDate.getSeconds());
    return time;
}
export function receiveSubtotal(subscriber, json) {
        return {type: RECEIVE_SUBTOTAL, subscriber: subscriber, total: json.totalEur, month: json.month, status: "Success", time: getTimeParsed() }
}

export function failedReceiveSubtotal(subscriber) {
    return {type: FAILED_RECEIVE_SUBTOTAL, subscriber: subscriber, status: "Failure", time: getTimeParsed() }

}

export function fetchSubtotal(subscriber) {
    return function (dispatch) {

        dispatch(requestSubtotal(subscriber))

        return fetch("http://localhost:8080/customers/" + subscriber)
            .then(function(response) {
                if (response.status >= 400 && response.status < 600) {
                    throw new Error("Bad response from server");
                }
                return response.json();
            })
            .then(json =>
                dispatch(receiveSubtotal(subscriber, json))
            )
            .catch(err => {
                dispatch(failedReceiveSubtotal(subscriber))
            });
    }
}

