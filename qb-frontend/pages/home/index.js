
import React, { PropTypes } from 'react';
import LiveTotalDisplay from '../../components/LiveTotalDisplay';
import ChangeSubscriberForm from '../../components/ChangeSubscriberForm';
import MetaInformation from '../../components/MetaInformation';

class App extends React.Component {

    render() {
        return (
            <div className="mdl-layout mdl-js-layout mdl-layout--fixed-header">
                <header className="mdl-layout__header">
                    <div className="mdl-layout__header-row">
                        <span className="mdl-layout-title">QB - Balance Checker</span>
                        <div className="mdl-layout-spacer"></div>
                        <nav className="mdl-navigation mdl-layout--large-screen-only">
                        </nav>
                    </div>
                </header>
                <div className="mdl-layout__drawer">
                    <span className="mdl-layout-title">Balance Checker</span>
                    <nav className="mdl-navigation">
                    </nav>
                </div>
                <main className="mdl-layout__content">
                    <div className="page-content">
                    <div className="mdl-grid">
                        <div className="mdl-cell mdl-cell--4-col mdl-cell--4-offset-desktop mdl-cell--2-offset-tablet" style={{textAlign: 'center'}}>
                                <MetaInformation />
                        </div>
                    </div>
                    <div className="mdl-grid">
                        <div className="mdl-cell mdl-cell--4-col mdl-cell--4-offset-desktop mdl-cell--2-offset-tablet" style={{textAlign: 'center'}}>
                            <LiveTotalDisplay/>
                        </div>
                    </div>
                        <div className="mdl-grid">
                            <div className="mdl-cell mdl-cell--4-col mdl-cell--4-offset-desktop mdl-cell--2-offset-tablet" style={{textAlign: 'center'}}>
                                <ChangeSubscriberForm/>
                            </div>
                        </div>
                    </div>
                </main>
            </div>
        );
    }
}

export default App