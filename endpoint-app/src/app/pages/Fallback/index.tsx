import React from 'react';
import {  withRouter } from 'react-router-dom';
import * as PropTypes from "prop-types";
import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar } from '@ionic/react';
import {ACCESS_TOKEN ,CONTEXT_PATH  } from '../../config';
import ExploreContainer from '../../components/ExploreContainer';
import './Fallback.css';
import * as AntD from 'antd';

class Fallback extends   React.Component<any, any>  {
  static contextTypes = { theme: PropTypes.object};
  static context: { theme: ReactUWP.ThemeType };
  constructor(props:any){
       super(props);
  }
  serviceName="";
  componentDidMount() {
  this.serviceName=this.props.location.search.replace("?id=","");
  console.log(this.serviceName);
  localStorage.setItem("secret",JSON.stringify({ secret:this.props.location.search.replace("?id=","") }));
//   this.props.handleCurrentUser(this.props.location.search.replace("?id=",""),this.props.history,
//           null,this);
  }

 render(){
    const { theme } = this.context;
    const antIcon = <AntD.Icon type="loading-3-quarters" style={{ fontSize: 30 }} spin />;
    const title=this.props.title?this.props.title:"Fallback Response : 500 Internal Server Error"
    const subTitle=this.props.subTitle?this.props.subTitle:("["+this.serviceName.toUpperCase()+"] Service is not available, Please contact an administrator for more support");
    console.log("Fallback",this.serviceName,title,subTitle);//style={{visibility:this.props.visible}}
      return (
        <div className="container"  >
          <strong>{title}</strong>
          <p><strong>{subTitle}</strong></p>
        </div>
      );
  };
};

export default withRouter(Fallback);
