import React from 'react';
import {  withRouter } from 'react-router-dom';
import * as PropTypes from "prop-types";
import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar } from '@ionic/react';
import {ACCESS_TOKEN ,CONTEXT_PATH  } from '../../config';
import ExploreContainer from '../../components/ExploreContainer';
import './SamplePage.css';
import * as AntD from 'antd';

class SamplePage extends   React.Component<any, any>  {
  static contextTypes = { theme: PropTypes.object};
  static context: { theme: ReactUWP.ThemeType };
  constructor(props:any){
       super(props);
  }
  componentDidMount() {
//   console.log(JSON.stringify({ secret:this.props.location.search.replace("?id=","") }));
  localStorage.setItem("secret",JSON.stringify({ secret:this.props.location.search.replace("?id=","") }));
  this.props.handleCurrentUser(this.props.location.search.replace("?id=",""),this.props.history,
          null,this);
  }

 render(){
    const { theme } = this.context;
    const antIcon = <AntD.Icon type="loading-3-quarters" style={{ fontSize: 30 }} spin />;
    console.log("Engine")
    return (
        <div className="container"  >
            <ExploreContainer />
        </div>
    );
  };
};

export default withRouter(SamplePage);
