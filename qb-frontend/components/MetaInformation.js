import React, { PropTypes } from 'react'
import { connect } from 'react-redux'

class MetaInformation extends React.Component {

    constructor(props) {
        super(props);
    }

    render() {

        return (
            <div>
                <h4>{this.props.subscriber} in {this.props.month}</h4>
            </div>
                );
    }
}

MetaInformation.propTypes = {
    month: PropTypes.string.isRequired,
    subscriber: PropTypes.string.isRequired,
}

const mapStateToProps = (state) => {
    return {
        subscriber: state.subscriber,
        month: state.month
    }
}

MetaInformation = connect(mapStateToProps)(MetaInformation)

export default MetaInformation