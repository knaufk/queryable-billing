import React, { PropTypes }  from 'react'
import { connect } from 'react-redux'
import { changeSubscriber } from '../core/actions'

class ChangeSubscriberForm extends React.Component {

    constructor(props) {
        super(props);
    }

    render() {
        let input
        return (
            <div>
                <form onSubmit={e => {
                    e.preventDefault()
                    if (!input.value.trim()) {
                        return
                    }
                    this.props.onClick(input.value)
                    input.value = ''
                }}>

                    <div className="mdl-textfield mdl-js-textfield">
                        <input className="mdl-textfield__input" type="text" ref={node => {
                            input = node
                        }}/>
                        <label className="mdl-textfield__label">Customer</label>
                    </div>
                    {/*<button type="submit" className="mdl-button mdl-js-button mdl-button--primary">*/}
                    {/*Change Subscriber*/}
                    {/*</button>*/}
                </form>

            </div>
        )

    }
}


ChangeSubscriberForm.propTypes = {
    subscriber: PropTypes.string.isRequired,
    onClick: PropTypes.func.isRequired
}

const mapStateToProps = (state) => {
    return {
        subscriber: state.subscriber,
    }
}

const mergeProps = (stateProps, dispatchProps, ownProps) => {
    const { subscriber, } = stateProps;
    const { dispatch } = dispatchProps;
    return {
        subscriber,
        onClick: (subscriber) => {
            dispatch(changeSubscriber(subscriber))
        }
    };
}

ChangeSubscriberForm = connect(mapStateToProps, null, mergeProps)(ChangeSubscriberForm)

export default ChangeSubscriberForm