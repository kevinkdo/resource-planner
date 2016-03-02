const Router = React.createClass({
  getInitialState() {
    var session = localStorage.getItem("session");
    return {
      route: session ? "reservation_list" : "login",
      view_id: 0
    };
  },

  render() {
    switch (this.state.route) {
      case "login":
        return <Login setPstate={this.setState.bind(this)} pstate={this.state} />
      case "group_manager":
        return <GroupManager setPstate={this.setState.bind(this)} pstate={this.state} />
      case "group_editor":
        return <GroupEditor setPstate={this.setState.bind(this)} pstate={this.state} id={this.state.view_id} />
      case "settings":
        return <Settings setPstate={this.setState.bind(this)} pstate={this.state} />
      case "reservation_list":
        return <ReservationList setPstate={this.setState.bind(this)} pstate={this.state} />
      case "reservation_creator":
        return <ReservationCreator setPstate={this.setState.bind(this)} pstate={this.state} resource_id={this.state.view_id} />
      case "reservation_editor":
        return <ReservationEditor setPstate={this.setState.bind(this)} pstate={this.state} id={this.state.view_id}/>
      case "resource_list":
        return <ResourceList setPstate={this.setState.bind(this)} pstate={this.state} />
      case "resource_creator":
        return <ResourceCreator setPstate={this.setState.bind(this)} pstate={this.state} />
      case "resource_editor":
        return <ResourceEditor setPstate={this.setState.bind(this)} pstate={this.state} id={this.state.view_id}/>
      case "permissions_manager":
        return <PermissionsManager setPstate={this.setState.bind(this)} pstate={this.state} id={this.state.view_id}/>
    }
    return <div>ERROR</div>;
  }
});
