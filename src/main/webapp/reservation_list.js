const ReservationList = React.createClass({
  getInitialState() {
    var now = new Date();
    var start = new Date();
    var end = new Date();
    start.setMonth(now.getMonth() - 1);
    end.setMonth(now.getMonth() + 1);
    var start_date = formatDate(start);
    var start_time = formatTime(start);
    var end_date = formatDate(end);
    var end_time = formatTime(end);   
    var error_msg = this.props.pstate.error_msg;
    var is_error = this.props.pstate.is_error;
    return {
      loading_tags: true,
      loading_table: true,
      subroute: "all",
      tags: [],
      start_date: start_date,
      start_time: start_time,
      end_date: end_date,
      end_time: end_time,
      all_reservations: {},
      pending_reservations: {},
      error_msg: error_msg,
      is_error: is_error
    };
  },

  cycleState(tag_name) {
    var tags = this.state.tags;
    tags.forEach(function(x) {
      if (x.name == tag_name) {
        if (x.state == "Required") x.state = "Excluded";
        else if (x.state == "Excluded") x.state = "";
        else x.state = "Required";
      }
    });
    this.setState({tags: tags});
  },

  set(field, value) {
    this.state[field] = value;
    this.setState(this.state);
  },

  getDateObject(dateStr, timeStr) {
    var date = new Date();
    var dateParts = dateStr.split('-');
    date.setFullYear(dateParts[0]);
    date.setMonth(dateParts[1]-1);
    date.setDate(dateParts[2]);
    
    var timeParts = timeStr.split(':');
    date.setHours(timeParts[0]);
    date.setMinutes(timeParts[1]);
    
    return date;
  },

  //http://stackoverflow.com/questions/1353684/detecting-an-invalid-date-date-instance-in-javascript
  isValidDate(date) {
  if ( Object.prototype.toString.call(date) !== "[object Date]" )
    return false;
  return !isNaN(date.getTime());
  },

  editReservation(id) {
    this.props.setPstate({
      route: "reservation_editor",
      view_id: id
    });
  },

  deleteReservation(id) {
    var me = this;
    send_xhr("DELETE", "/api/reservations/" + id, sessionStorage.getItem("session"), null,
      function(obj) {
        me.refresh();
        me.setState({error_msg: ""});
      },
      function(obj) {
        me.refresh();
        me.setState({error_msg: obj.error_msg, is_error: true});
      }
    );
  },

  approveReservation(id, approve_boolean) {
    var me = this;
    send_xhr("GET", "/api/reservations/canceledWithApproval/" + id, sessionStorage.getItem("session"), null,
      function(obj) {
        var confirmed_approve = ((obj.data.length == 0) || !approve_boolean);
        if (!confirmed_approve) {
          confirmed_approve = confirm("By approving this reservation, you will necessarily be cancelling " + obj.data.length.toString() + " other reservations. Proceed?");
        }

        if (confirmed_approve) {
          send_xhr("POST", "/api/reservations/approveReservation/" + id, sessionStorage.getItem("session"), JSON.stringify({approved: approve_boolean}),
            function(obj) {
              me.refresh();
              me.setState({error_msg: obj.error_msg, is_error: false});
            },
            function(obj) {
              me.refresh();
              me.setState({error_msg: obj.error_msg, is_error: true});
            }
          );
        }
      },
      function(obj) {
        me.refresh();
        me.setState({error_msg: obj.error_msg, is_error: true});
      }
    );
  },

  refresh() {
    var me = this;
    var required_tags_str = this.state.tags.filter(x => x.state=="Required").map(x => x.name).join(",");
    var excluded_tags_str = this.state.tags.filter(x => x.state=="Excluded").map(x => x.name).join(",");
    var xhr1 = send_xhr("GET", "/api/reservations/?start=" + round(this.getDateObject(this.state.start_date, this.state.start_time)).toISOString() + "&end=" + round(this.getDateObject(this.state.end_date, this.state.end_time)).toISOString() + "&required_tags=" + required_tags_str + "&excluded_tags=" + excluded_tags_str, sessionStorage.getItem("session"), null,
      function(obj) {
        var new_reservations = {};
        obj.data.reservations.forEach(function(x) {
          new_reservations[x.reservation_id] = x;
        });
        me.setState({
          all_reservations: new_reservations,
          loading_table: false,
          subroute: "all"
        });
      },
      function(obj) {
        me.setState({
          loading_table: false,
          error_msg: obj.error_msg,
          is_error: true
        });
      }
    );
    var xhr2 = send_xhr("GET", "/api/reservations/approvableReservations", sessionStorage.getItem("session"), null,
      function(obj) {
        var new_reservations = {};
        obj.data.reservations.forEach(function(x) {
          new_reservations[x.reservation_id] = x;
        });
        me.setState({
          pending_reservations: new_reservations,
          loading_table: false,
          subroute: "all"
        });
      },
      function(obj) {
        me.setState({
          loading_table: false,
          error_msg: obj.error_msg,
          is_error: true
        });
      }
    );
    this.reqs.push(xhr1, xhr2);
  },

  componentDidMount() {
    var me = this;
    this.reqs = [];
    this.props.setPstate({error_msg: ""});
    this.refresh();
    var xhr = send_xhr("GET", "/api/tags", sessionStorage.getItem("session"), null,
      function(obj) {
        me.setState({
          tags: obj.data.tags.map(x => ({name: x, state: ""})),
          loading_tags: false
        });
      },
      function(obj) {
        me.setState({
          loading_tags: false,
          error_msg: obj.error_msg,
          is_error: true
        });
      }
    );
    this.reqs.push(xhr);
  },

  componentWillUnmount() {
    this.reqs.forEach((req) => req.abort());
  },

  render() {
    var me = this;
    var leftpane =
      <div>
        <h3></h3>
        <div className={"panel panel-primary" + (me.state.subroute=='pending' ? " grayout" : "")}>
          <div className="panel-heading">
            <h3 className="panel-title">Display settings</h3>
          </div>
          <div className="panel-body">
            <button type="button" className={"btn btn-primary" + (me.state.subroute=='pending' ? " disabled" : "")} onClick={this.refresh}>Load reservations</button>
            <h4>Start</h4>
              <input type="date" className="form-control" id="reservation_list_start_date" value={this.state.start_date} onChange={(evt) => this.set('start_date', evt.target.value)} disabled={me.state.subroute=='pending'}/>
              <input type="time" className="form-control" id="reservation_list_start_time" value={this.state.start_time} onChange={(evt) => this.set('start_time', evt.target.value)} disabled={me.state.subroute=='pending'}/>
            <h4>End</h4>
              <input type="date" className="form-control" id="reservation_list_end_date" value={this.state.end_date} onChange={(evt) => this.set('end_date', evt.target.value)} disabled={me.state.subroute=='pending'}/>
              <input type="time" className="form-control" id="reservation_list_end_time" value={this.state.end_time} onChange={(evt) => this.set('end_time', evt.target.value)} disabled={me.state.subroute=='pending'}/>
            <h4>Tags</h4>
            {this.state.loading_tags ? <Loader /> : <div>
              <ul className="list-group">
                {this.state.tags.map(x =>
                  <a key={"reservationtag" + x.name} className={"list-group-item pointer" + (me.state.subroute=='pending' ? " disabled" : "")} onClick={(evt) => {evt.preventDefault(); me.cycleState(x.name)} }>{x.name}<span className="badge">{x.state}</span></a>
                )}
              </ul>
              {Object.keys(me.state.tags).length > 0 ? null :
                  <div className="lead text-center">No tags to display</div>}
              </div>
            }
          </div>
        </div>
      </div>
    var rightpane_all = this.state.loading_table ? <Loader /> : (
      <div>
        <ul className="nav nav-tabs">
          <li className={this.state.subroute == 'all' ? "active" : ""}><a href="#reservation_list/0" onClick={(evt) => this.setState({subroute: "all"})}>All</a></li>
          <li className={this.state.subroute == 'pending' ? "active" : ""}><a href="#reservation_list/0" onClick={(evt) => this.setState({subroute: "pending"})}>Pending your approval</a></li>
        </ul>
        <table className="table table-hover">
          <thead>
            <tr>
              <th>ID</th>
              <th>Title</th>
              <th>Complete</th>
              <th>Resources</th>
              <th>User</th>
              <th>Start</th>
              <th>End</th>
              <th></th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {Object.keys(me.state.all_reservations).map(id => {
              var x = me.state.all_reservations[id];
              return <tr key={"reservation " + x.reservation_id} className={x.complete ? "success" : "danger"}>
                <td>{x.reservation_id}</td>
                <td>{x.title}</td>
                <td>{x.complete ? "Yes" : "No"}</td>
                <td>{x.resources.map(x => x.name).join(",")}</td>
                <td>{x.user.username}</td>
                <td>{new Date(x.begin_time).toLocaleString()}</td>
                <td>{new Date(x.end_time).toLocaleString()}</td>
                <td><a role="button" onClick={() => this.editReservation(x.reservation_id)}>View/Edit</a></td>
                <td><a role="button" onClick={() => this.deleteReservation(x.reservation_id)}>Delete</a></td>
              </tr>
            })}
            {Object.keys(me.state.all_reservations).length > 0 ? null :
              <tr><td className="lead text-center" colSpan="7">No reservations in this timespan</td></tr>}
          </tbody>
        </table>
      </div>
    );
    var rightpane_pending = this.state.loading_table ? <Loader /> : (
      <div>
        <ul className="nav nav-tabs">
          <li className={this.state.subroute == 'all' ? "active" : ""}><a href="#reservation_list/0" onClick={(evt) => this.setState({subroute: "all"})}>All</a></li>
          <li className={this.state.subroute == 'pending' ? "active" : ""}><a href="#reservation_list/0" onClick={(evt) => this.setState({subroute: "pending"})}>Pending your approval</a></li>
        </ul>
        <table className="table table-hover">
          <thead>
            <tr>
              <th>ID</th>
              <th>Title</th>
              <th>Resources</th>
              <th>User</th>
              <th>Start</th>
              <th>End</th>
              <th></th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {Object.keys(me.state.pending_reservations).map(id => {
              var x = me.state.pending_reservations[id];
              return <tr key={"reservation " + x.reservation_id}>
                <td>{x.reservation_id}</td>
                <td>{x.title}</td>
                <td>{x.resources.map(x => x.name).join(",")}</td>
                <td>{x.user.username}</td>
                <td>{new Date(x.begin_time).toLocaleString()}</td>
                <td>{new Date(x.end_time).toLocaleString()}</td>
                <td><a role="button" onClick={() => this.approveReservation(x.reservation_id, true)}>Approve</a></td>
                <td><a role="button" onClick={() => this.approveReservation(x.reservation_id, false)}>Deny</a></td>
              </tr>
            })}
            {Object.keys(me.state.pending_reservations).length > 0 ? null :
              <tr><td className="lead text-center" colSpan="7">No reservations to approve</td></tr>}
          </tbody>
        </table>
      </div>
    );
    return (
      <div>
        <Navbar setPstate={this.props.setPstate} pstate={this.props.pstate}/>

        <div className="container">
          <div className="row">
            <div className="col-xs-4 col-md-3">
              {leftpane}
            </div>
            <div className="col-xs-8 col-md-9">
              <h3>Reservations <button type="button" className="btn btn-success pull-right" onClick={() => this.props.setPstate({route: "reservation_creator"})}><span className="glyphicon glyphicon-time" aria-hidden="true"></span> New reservation</button></h3>
              {!this.state.error_msg ? <div></div> :
                <div className={"alert " + (this.state.is_error ? "alert-danger" : "alert-success")}>
                  <strong>{this.state.error_msg}</strong>
                </div>
              }
              {me.state.subroute == 'all' ? rightpane_all : rightpane_pending}
            </div>
          </div>
        </div>
      </div>
    )
  }
});
