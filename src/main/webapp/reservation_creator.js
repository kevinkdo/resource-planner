const ReservationCreator = React.createClass({
  createReservation(evt) {
    evt.preventDefault();
    var me = this;
    this.setState({sending: true});
    send_xhr("POST", "/api/reservations", sessionStorage.getItem("session"),
      JSON.stringify({user_id: this.state.user_id, resource_ids:this.state.resource_ids, begin_time: round(this.getDateObject(this.state.start_date, this.state.start_time)).toISOString(), end_time: round(this.getDateObject(this.state.end_date, this.state.end_time)).toISOString(), should_email:this.state.should_email, title: this.state.title, description: this.state.description}),
      function(obj) {
        me.props.setPstate({ route: "reservation_list", is_error: false, error_msg: "Successfully created reservation!" });
      },
      function(obj) {
        me.setState({sending: false, error_msg: obj.error_msg, is_error: true});
      }
    );
  },

  cancel() {
    this.props.setPstate({ route: "reservation_list" });
  },

  set(field, value) {
    this.state[field] = value;
    this.setState(this.state);
  },

  addResource() {
    if (this.state.all_resources.length > 0) {
      this.state.resource_ids.push(this.state.all_resources[0].resource_id);
      this.setState({resource_ids: this.state.resource_ids});
    }
  },

  removeResource() {
    this.state.resource_ids.pop();
    this.setState({resource_ids: this.state.resource_ids});
  },

  setResourceIdAtIndex(value, index) {
    this.state.resource_ids[index] = value;
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

  getInitialState() {
    var now = new Date();
    var end = new Date();
    end.setHours(end.getHours() + 1);
    var start_date = formatDate(now);
    var start_time = formatTime(now);
    var end_date = formatDate(end);
    var end_time = formatTime(end);
    return {
      sending: false,
      all_resources: [],
      resource_ids: this.props.pstate.resource_ids.length > 0 ? this.props.pstate.resource_ids : [],
      user_id: userId(),
      title: "",
      description: "",
      start_date: start_date,
      start_time: start_time,
      end_date: end_date,
      end_time: end_time,
      should_email: false,
      error_msg: "",
      is_error: false,
      initial_load_resources: true
    };
  },

  componentDidMount() {
    var me = this;
    send_xhr("GET", "/api/resources/", sessionStorage.getItem("session"), null,
      function(obj) {
        if (obj.data.resources.length == 0) {
          me.setState({initial_load_resources: false, error_msg: "No resources to reserve!", is_error: true, all_resources: obj.data.resources});
        } else {
          me.setState({
            all_resources: obj.data.resources,
            resource_ids: me.state.resource_ids.length > 0 ? me.state.resource_ids : [obj.data.resources[0].resource_id],
            initial_load_resources: false
          });
        }
      },
      function(obj) {
        me.setState({initial_load_resources: false, error_msg: obj.error_msg, is_error: true});
      }
    );
  },

  render() {
    var me = this;
    var form = this.state.initial_load_resources ? <Loader /> :
      <div className="container">
        <div className="row">
          <div className="col-md-6 col-md-offset-3">
            <form>
              <legend>New reservation</legend>
              {!this.state.error_msg ? <div></div> :
                <div className={"alert " + (this.state.is_error ? "alert-danger" : "alert-success")}>
                  <strong>{this.state.error_msg}</strong>
                </div>
              }
              <div className="form-group">
                <label htmlFor="reservation_creator_resource">Resources</label>
                <div>
                  <button type="button" className="btn btn-link" onClick={this.addResource}>Add a resource to this reservation</button>
                  <button type="button" className="btn btn-link" onClick={this.removeResource}>Remove a resource from this reservation</button>
                </div>
                {me.state.resource_ids.map((resource_id, index) =>
                  <select key={"select number" + index.toString()} className="form-control" defaultValue={resource_id} onChange={(evt)=>this.setResourceIdAtIndex(evt.target.value, index)}>
                    {me.state.all_resources.map(x =>
                      <option key={x.resource_id} value={x.resource_id}>{x.name}</option>
                    )}
                  </select>
                )}
              </div>
              {/*<div className="form-group">
                <label htmlFor="reservation_creator_user_id">Reserving User's ID (yours by default)</label>
                <input type="number" className="form-control" id="reservation_creator_user_id" placeholder="User ID" value={this.state.user_id} onChange={(evt)=>this.set("user_id", evt.target.value)}/>
              </div>*/}
              <div className="form-group">
                <label>Title</label>
                <input type="text" className="form-control" placeholder="Title" value={this.state.title} onChange={(evt)=>this.set("title", evt.target.value)} />
              </div>
              <div className="form-group">
                <label>Description</label>
                <input type="text" className="form-control" placeholder="Description" value={this.state.description} onChange={(evt)=>this.set("description", evt.target.value)} />
              </div>
              <div className="form-group">
                <label htmlFor="reservation_creator_start_date">Start Date</label>
                <input type="date" className="form-control" id="reservation_creator_start_date" value={this.state.start_date} onChange={(evt)=>this.set("start_date", evt.target.value)} />
              </div>
              <div className="form-group">
                <label htmlFor="reservation_creator_start_time">Start Time</label>
                <input type="time" className="form-control" id="reservation_creator_start_time" value={this.state.start_time} onChange={(evt)=>this.set("start_time", evt.target.value)}/>
              </div>
              <div className="form-group">
                <label htmlFor="reservation_creator_end_date">End Date</label>
                <input type="date" className="form-control" id="reservation_creator_end_date" value={this.state.end_date} onChange={(evt)=>this.set("end_date", evt.target.value)} />
              </div>
              <div className="form-group">
                <label htmlFor="reservation_creator_end_time">End Time</label>
                <input type="time" className="form-control" id="reservation_creator_end_time" value={this.state.end_time} onChange={(evt)=>this.set("end_time", evt.target.value)}/>
              </div>
              <div className="checkbox">
                <label htmlFor="reservation_creator_should_email"><input type="checkbox" id="reservation_creator_should_email" checked={this.state.should_email} onChange={(evt)=>this.set("should_email", evt.target.checked)}/> Email reminder</label>
              </div>
              <div className="btn-toolbar">
                <button type="submit" className="btn btn-primary" onClick={this.createReservation} disabled={this.state.sending}>Create reservation</button>
                <button type="button" className="btn btn-default" onClick={this.cancel}>Cancel</button>
              </div>
            </form>
          </div>
        </div>
      </div>;
    return (
      <div>
        <Navbar setPstate={this.props.setPstate} pstate={this.props.pstate}/>
        {form}
      </div>
    )
  }
});
