// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Actions from '../actions.js';
import Logo from '../../assets/images/EMC_DELL_LOGO.svg';
import LogoShield from '../../assets/images/logo.svg';
import Isvg from 'react-inlinesvg';

var LogIn = React.createClass({

	setUsername: function(e){
		Actions.setLoginUsername.bind(this, e.target.value)();
	},

	setPassword: function(e){
		Actions.setLoginPassword.bind(this, e.target.value)();
	},

	userClickOnLogin: function(){
		Actions.login();
	},

	componentDidMount: function(){
		var container = document.querySelector('.login-container');
		container.addEventListener('keydown', function(event) {
    	if(event.keyCode === 13){
				Actions.login();
			}
    }, false);
	},

	render: function(){

		return(

			<div className="login-container">

				<div className="title">
					<div className="content">
						<br/>
						Ocopea
					</div>
				</div>

				<div className="inside">
					<div className="intro">
						welcome to ocopea! please sign in with your credentials
					</div>
					<div className="inputs">

						<input
							autoComplete='off'
							type="text"
							name="username"
							onChange={this.setUsername}
							placeholder="Admin username"/>

						<input
							type="password"
							name="password"
							onChange={this.setPassword}
							placeholder="Admin password"/>

					</div>

					<button
						className="button button-primary"
						onClick={this.userClickOnLogin}>login</button>

				</div>

			</div>

		)

	}

});

export default LogIn;

// <div className="forgot-password">
// 	forgot password
// </div>
