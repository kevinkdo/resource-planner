const AdminConsole = React.createClass({
  set(field, value) {
    this.state[field] = value;
	this.setState(this.state);
  },

  createUser(evt) {
   	evt.preventDefault();
	var me = this; this.setState({loading: true});
	send_xhr("POST", "/api/users", localStorage.getItem("session"),
	  JSON.stringify({username:this.state.username, password:this.state.password, email: this.state.email, should_email: this.state.should_email}),		
      function(obj) {		
        me.props.setPstate({ route: "reservation_list" });		
      },		
      function(obj) {		
        me.setState({loading: false, error_msg: obj.error_msg});		
      }		
    );		
  },

  cancel() {
  	this.props.setPstate({
  	  route: "reservation_list"
  	});
  },

  getInitialState() {
  	return {
  	  email: "",
  	  username: "",
  	  password: "",
  	  should_email: "false",
  	  loading: false,
  	  error_msg: ""
  	};
  },

  render() {		  
    return (		     
      <div>		      
          <Navbar setPstate={this.props.setPstate} pstate={this.props.pstate}/>		
  		  
          <div className="container">	
           <div className="row">		 
             <div className="col-md-6 col-md-offset-3">		
               <form>		
                 <legend>New user</legend>		
                 {!this.state.error_msg ? <div></div> :		
                   <div className="alert alert-danger">		
                     <strong>{this.state.error_msg}</strong>		
                   </div>		
                 }		
                 <div className="form-group">		
                   <label htmlFor="user_creator_email">Email</label>		
                   <input type="email" className="form-control" id="user_creator_email" placeholder="Email" value={this.state.email} onChange={(evt)=>this.set("email", evt.target.value)}/>		
                 </div>		
                 <div className="form-group">		
                   <label htmlFor="user_creator_username">Username</label>		
                   <input type="text" className="form-control" id="user_creator_username" placeholder="Username" value={this.state.username} onChange={(evt)=>this.set("username", evt.target.value)}/>		
                 </div>		
                 <div className="form-group">		
                   <label htmlFor="user_creator_password">Password</label>		
                   <input type="password" className="form-control" id="user_creator_username" placeholder="Password" value={this.state.password} onChange={(evt)=>this.set("password", evt.target.value)}/>		
                 </div>		
                 <div className="checkbox">		
                   <label htmlFor="user_creator_should_email"><input type="checkbox" id="user_creator_should_email" checked={this.state.should_email} onChange={(evt)=>this.set("should_email", evt.target.checked)}/>Email reminders</label>		
                 </div>		
                 <div className="btn-toolbar">		
                   <button type="submit" className="btn btn-primary" onClick={this.createUser}>Create user</button>		
                   <button type="button" className="btn btn-default" onClick={this.cancel}>Cancel</button>		
                 </div>		
               </form>		
             </div>		
           </div>		
          </div>		         
        </div>		      
      );		  
   }		  
});