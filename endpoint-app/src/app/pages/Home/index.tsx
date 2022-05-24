import React from 'react';
import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar } from '@ionic/react';
import ExploreContainer from '../../components/ExploreContainer';
import './Home.css';
import * as AntD from 'antd';

class Home extends   React.Component<any, any>  {
 render(){
    const { theme } = this.context;
    const antIcon = <AntD.Icon type="loading-3-quarters" style={{ fontSize: 30 }} spin />;
    console.log("Home")
    return (
      <div className="container"  >
          <ExploreContainer />
      </div>
    );
  };
};

export default Home;
