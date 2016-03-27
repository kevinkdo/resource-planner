const Router = React.createClass({
  getInitialState() {
    var session = localStorage.getItem("session");
    return {
      route: session ? "reservation_list" : "login",
      view_id: 0
    };
  },

  setStateFromLocationHash() {
    var hash = location.hash.substring(1);
    var slash_index = hash.search('/');
    var view_id = Number(hash.substring(slash_index + 1));
    this.setState({
      route: hash.substring(0, slash_index),
      view_id: view_id
    });
  },

  //This is kind of confusing because there are two ways
  //that sub components can change the route. One is by
  //changing the hash with an href attribute on a link.
  //The second is by calling setStateWrapper (passed down as
  //setPstate prop)
  componentDidMount() {
    window.onhashchange = this.setStateFromLocationHash;
    if (!location.hash) {
      location.hash = '#' + this.state.route + '/' + this.state.view_id.toString();
    } else {
      this.setStateFromLocationHash();
    }
  },

  setStateWrapper(data) {
    if (!data.view_id) {
      data.view_id = 0;
    }
    if (data.route) {
      location.hash = '#' + data.route + '/' + data.view_id.toString();
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
