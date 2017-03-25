import React, { PropTypes } from 'react'
import { connect } from 'react-redux'
import { fetchSubtotal } from '../core/actions'
import s from './custom.css'
class LiveTotalDisplay extends React.Component {

    constructor(props) {
        super(props);
        this.state = {timer: null};
    }

    componentWillMount() {
        var timer = setTimeout(this.props.onMount,2000)
        this.setState({timer: timer});
    }

    componentWillReceiveProps(nextProps) {
        if (this.props.subscriber != nextProps.subscriber) {
            console.log("Subscriber changed")
            clearTimeout(this.state.timer)
        }

        if (!nextProps.isFetching) {
            console.log("Not fetching, fetching again after timeout.")
            var timer = setTimeout(nextProps.onMount,2000)
            this.setState({timer: timer});
        }
    }

    render() {
        let statusClassName;
        if (this.props.lastFetchStatus == "Success") {
            statusClassName = s.success;
        } else {
            statusClassName = s.failure;
        }

        return (
            <div>
                <h4>€ {this.props.total}</h4>
                <div className={statusClassName}>{this.props.lastFetchTime}</div>
            </div>
        );
    }
}

LiveTotalDisplay.propTypes = {
    subscriber: PropTypes.string.isRequired,
    total: PropTypes.string.isRequired,
    lastFetchStatus: PropTypes.string.isRequired,
    lastFetchTime: PropTypes.string.isRequired,
    isFetching: PropTypes.bool.isRequired,
    onMount: PropTypes.func.isRequired
}

const mapStateToProps = (state) => {
    return {
        subscriber: state.subscriber,
        lastFetchStatus: state.lastFetch.status,
        lastFetchTime: state.lastFetch.time,
        isFetching: state.isFetching,
        total: state.total,
    }
}

const mergeProps = (stateProps, dispatchProps, ownProps) => {
    const { subscriber, total, isFetching, lastFetchStatus,lastFetchTime } = stateProps;
    const { dispatch } = dispatchProps;
    return {
        subscriber,
        total,
        isFetching,
        lastFetchStatus,
        lastFetchTime,
        onMount: () => {
            dispatch(fetchSubtotal(subscriber))
        }
    };
}


LiveTotalDisplay = connect(mapStateToProps,null, mergeProps)(LiveTotalDisplay)

export default LiveTotalDisplay