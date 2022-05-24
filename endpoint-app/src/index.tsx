import React from 'react';
import ReactDOM from 'react-dom';
import App from './app/App';
import * as serviceWorkerRegistration from './serviceWorkerRegistration';
import reportWebVitals from './reportWebVitals';
import { Theme as UWPThemeProvider, getTheme } from "react-uwp/Theme";
import background from './theme/ls-97.jpg';
import { HashRouter as Router } from 'react-router-dom';

ReactDOM.render(
    <UWPThemeProvider
        style={{display: 'contents !important'}}
        theme={getTheme({
          themeName: "dark", // set custom theme
          accent: "black", // set accent color
          useFluentDesign: true, // sure you want use new fluent design.
          desktopBackgroundImage: background// set global desktop background image
        })}
      >
        <Router  >
            <App />
        </Router>
    </UWPThemeProvider>,
  document.getElementById('root')
);

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://cra.link/PWA
serviceWorkerRegistration.unregister();

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
