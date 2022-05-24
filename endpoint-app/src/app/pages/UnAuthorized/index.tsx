import React from 'react';
import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar } from '@ionic/react';
import ExploreContainer from '../../components/ExploreContainer';
import {  withRouter } from 'react-router-dom';
import './UnAuthorized.css';

class UnAuthorized extends   React.Component<any, any>  {

 render(){
    console.log('UnAuthorized',this.props);
  return (

            <div className="container"  >
                <strong>Content Menu is Unauthorized</strong>
                <p><strong>Please try to login again or contact an administrators for more supports</strong></p>
            </div>
  );
  };
};

export default withRouter(UnAuthorized);
