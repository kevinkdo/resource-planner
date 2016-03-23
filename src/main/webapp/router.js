const Router = React.createClass({
  getInitialState() {
    var session = localStorage.getItem("session");
    return {
      route: session ? "reservation_list" : "login",
      view_id: 0
    };
  },

  onhashchange() {
    this.setState({
      route: location.hash.substring(1)
    });
  },

  componentDidMount() {
    window.onhashchange = this.onhashchange;
    if (!location.hash) {
      location.hash = '#' + this.state.route;
    } else {
      this.setState({
        route: location.hash.substring(1)
      });
    }
  },

  setStateWrapper(data) {
    if (data.route) {
      location.hash = '#' + data.route;
    }
    this.setState(data);
  },

  render() {
    switch (this.state.route) {
      case "login":
        return <Login setPstate={this.setStateWrapper.bind(this)} pstate={this.state} />
      case "group_manager":
        return <GroupManager setPstate={this.setStateWrapper.bind(this)} pstate={this.state} />
      case "group_editor":
        return <GroupEditor setPstate={this.setStateWrapper.bind(this)} pstate={this.state} />
      case "settings":
        return <Settings setPstate={this.setStateWrapper.bind(this)} pstate={this.state} />
      case "reservation_list":
        return <ReservationList setPstate={this.setStateWrapper.bind(this)} pstate={this.state} />
      case "reservation_creator":
        return <ReservationCreator setPstate={this.setStateWrapper.bind(this)} pstate={this.state} />
      case "reservation_editor":
        return <ReservationEditor setPstate={this.setStateWrapper.bind(this)} pstate={this.state} />
      case "resource_list":
        return <ResourceList setPstate={this.setStateWrapper.bind(this)} pstate={this.state} />
      case "resource_creator":
        return <ResourceCreator setPstate={this.setStateWrapper.bind(this)} pstate={this.state} />
      case "resource_editor":
        return <ResourceEditor setPstate={this.setStateWrapper.bind(this)} pstate={this.state} />
      case "permissions_manager":
        return <PermissionsManager setPstate={this.setStateWrapper.bind(this)} pstate={this.state} />
      case "admin_console":
        return <AdminConsole setPstate={this.setStateWrapper.bind(this)} pstate={this.state} />
    }
    return <div>ERROR</div>;
  }
});
