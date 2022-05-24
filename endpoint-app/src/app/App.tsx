import React from 'react';
import * as PropTypes from "prop-types";
import { Redirect,Switch, Route,withRouter,BrowserRouter as Router } from 'react-router-dom';
import { IonApp, IonRouterOutlet } from '@ionic/react';
import { IonReactRouter } from '@ionic/react-router';
import {ACCESS_TOKEN,CONTEXT_PATH  } from './config';
import Home from './pages/Home';
import UnAuthorized from './pages/UnAuthorized';
import Fallback from './pages/Fallback';
import {APIroutes} from './routes/APIRoute';
import {utilities} from './utilities';

/* Core CSS required for Ionic components to work properly */
import '@ionic/react/css/core.css';

/* Basic CSS for apps built with Ionic */
import '@ionic/react/css/normalize.css';
import '@ionic/react/css/structure.css';
import '@ionic/react/css/typography.css';

/* Optional CSS utils that can be commented out */
import '@ionic/react/css/padding.css';
import '@ionic/react/css/float-elements.css';
import '@ionic/react/css/text-alignment.css';
import '@ionic/react/css/text-transformation.css';
import '@ionic/react/css/flex-utils.css';
import '@ionic/react/css/display.css';

/* Theme variables */
import '../theme/variables.css';
import './AppIonic.css';
import './AppAntd.css';

// const socket = SockJS(SOCK_WS);
// const ws = new Client(socket);
class App extends   React.Component<any, any> {
public history:any;
static contextTypes = { theme: PropTypes.object};
static context: { theme: ReactUWP.ThemeType };
constructor(props:any){
       super(props);
       this.state = {
            hostTheme:{},
            isFailed:false,
            isLoading:false,
            handleCurrentUser:this.handleCurrentUser.bind(this),
            handleSetTheme:this.handleSetTheme.bind(this)
       }

    }
componentDidMount() {
}

handleSetTheme=(e:any)=>{
         if(e.data !==''){
            this.setState({hostTheme:e.data});
        }
    }
loadCurrentUser(e:any,c:any,i:any){
    APIroutes.LoginServices.getCurrentUser(e)
    .then((response:any)  => {
//          console.log("APPS getCurrentUser  :",response,response.data['user_name'])
        if(typeof(response.data.error)==='undefined'){
             localStorage.setItem(ACCESS_TOKEN,response.data['access_token']);
             localStorage.setItem("currentUser",response.data['user_name']);
             if(c!=null) c(i,null);

            //console.log("APPS getCurrentUser bb  :",localStorage.getItem(ACCESS_TOKEN),localStorage.getItem('secret'))
        }else{
//             console.log("loadDocumentTree error response :",response)
            utilities.RouteHelper.statusMessage(response.data.status,response.data.message,'linear-gradient(to bottom,  #0a1a89 0%, #F6f7ef  100%)  repeat scroll 0 0 rgba(0, 0, 0, 0)');
            window.parent.postMessage({message: 'Form is Close', show: false}, '*');
            this.history.replace(CONTEXT_PATH()+'401');
            this.setState({
                isLoading: false,
                isFailed:true
            });
        }
//         localStorage.setItem('current_user',JSON.stringify(response));
    }).catch((error:any) => {
 //       console.log("APPS getCurrentUser error :"+error)
      this.setState({
         isLoading: false,
         isFailed:true
      });
      window.parent.postMessage({message: 'Form is Close', show: false}, '*');
      this.history.replace(CONTEXT_PATH()+'401');
    });
}
handleCurrentUser(e:any,history:any,callback:any,i:any){
    this.history=history;
    localStorage.removeItem(ACCESS_TOKEN);

    localStorage.removeItem("secret");
//     console.log("handleCurrentUser",e,localStorage.getItem("secret"));
    if(localStorage.getItem("secret")==null){
       //console.log('handleCurrentUser null',localStorage.getItem("secret"),e);
        localStorage.setItem("secret",JSON.stringify({secret:e}));
        localStorage.setItem(ACCESS_TOKEN,e);
    }
// console.log('handleCurrentUser',localStorage.getItem("secret"));
    let token=JSON.parse(JSON.parse(JSON.stringify(localStorage.getItem("secret"))));
    //console.log('handleCurrentUsermmm ',token,e);
    if(token!==localStorage.getItem("secret") && !this.state.isFailed){
        this.loadCurrentUser(token,callback,i);
    }else
    {
    }
}
render(){
 console.log('APP RENDER X',this,CONTEXT_PATH());
 const { theme } = this.context;

return(
  <IonApp>
      <Switch>
        <Route exact path={CONTEXT_PATH()+"home"}>
          <Home {...this.props} {...this.state} />
        </Route>
        <Route exact  path={CONTEXT_PATH()+"fallback"}>
          <Fallback {...this.props} {...this.state}/>
        </Route>
        <Route   path={CONTEXT_PATH()+"fallback/?id=:token"}>
          <Fallback {...this.props} {...this.state}/>
        </Route>
        <Route   path={CONTEXT_PATH()+"fallback/:token"}>
          <Fallback {...this.props} {...this.state}/>
        </Route>
        <Route exact  path={CONTEXT_PATH()+"401"}>
          <UnAuthorized {...this.props} {...this.state}/>
        </Route>
        <Route exact path={CONTEXT_PATH()}>
          <Redirect to={CONTEXT_PATH()+"home"} />
        </Route>
      </Switch>
  </IonApp>
  );
  }
};

export default withRouter(App);
