import { combineReducers } from 'redux'
import { CHANGE_SUBSCRIBER, FETCH_SUBTOTAL, RECEIVE_SUBTOTAL, FAILED_RECEIVE_SUBTOTAL} from './actions'

function state(state = {subscriber: "Emma", total: "-", month: "-", isFetching: false, lastFetch: {status: 'Success', time: 'Not fetched yet.'}, }, action) {

    switch (action.type) {
        case CHANGE_SUBSCRIBER:
            return Object.assign({}, state, {
                subscriber: action.subscriber,
                total: "-",
                month: "-",
                lastFetch: {status: 'Success', time: 'Not fetched yet.'}
            })
        case FETCH_SUBTOTAL:
            return Object.assign({}, state, {
                isFetching: true
            })
        case RECEIVE_SUBTOTAL:
            if (state.subscriber == action.subscriber) {
                return Object.assign({}, state, {
                    isFetching: false,
                    total: action.total,
                    month: action.month,
                    lastFetch: {status: action.status, time: action.time}
                })
            } else {
                return state
            }
        case FAILED_RECEIVE_SUBTOTAL:
            return Object.assign({}, state, {
                isFetching: false,
                lastFetch: {status: action.status, time: action.time}
            })
        default:
            return state
    }
}


export default state
