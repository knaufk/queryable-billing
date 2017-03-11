import React from 'react'
import { connect } from 'react-redux'
import { changeSubscriber } from '../core/actions'

let ChangeSubscriberForm = ({ dispatch }) => {
    let input

    return (
        <div>
            <form onSubmit={e => {
                e.preventDefault()
                if (!input.value.trim()) {
                    return
                }
                dispatch(changeSubscriber(input.value))
                input.value = ''
            }}>
                <div className="mdl-textfield mdl-js-textfield">
                    <input className="mdl-textfield__input" type="text"  ref={node => {
                        input = node
                    }} />
                    <label className="mdl-textfield__label">Full Name...</label>
                </div>
                <button type="submit" className="mdl-button mdl-js-button mdl-button--primary">
                    Change Subscriber
                </button>
            </form>

        </div>
    )
}



ChangeSubscriberForm = connect()(ChangeSubscriberForm)

export default ChangeSubscriberForm