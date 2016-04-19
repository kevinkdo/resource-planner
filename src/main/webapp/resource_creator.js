const ResourceCreator = React.createClass({
  createResource(evt) {
    evt.preventDefault();
    var me = this;
    this.setState({sending: true});
    send_xhr("POST", "/api/resources", localStorage.getItem("session"),
      JSON.stringify({restricted: this.state.restricted, name:this.state.name, description:this.state.description, tags: this.state.tags.filter(x => x.length > 0), parent_id: this.state.parent_id, shared_count: this.state.shared_count}),
      function(obj) {
        me.props.setPstate({ route: "resource_list", is_error: false, error_msg: "Successfully created resource!" });
      },
      function(obj) {
        me.setState({sending: false, error_msg: obj.error_msg, is_error: true});
      }
    );
  },

  cancel() {
    this.props.setPstate({ route: "resource_list" });
  },

  addTag() {
    this.state.tags.push("");
    this.setState({tags: this.state.tags});
  },

  set(field, value) {
    this.state[field] = value;
    this.setState(this.state);
  },

  setTag(evt, i) {
    this.state.tags[i] = evt.target.value;
    this.setState({tags: this.state.tags});
  },

  removeTag(evt, i) {
    this.state.tags.splice(i, 1);
    this.setState({tags: this.state.tags});
  },

  getInitialState() {
    return {
      all_resources: [{resource_id: 0, name: "No parent object"}],
      initial_load_resources: true,
      name: "",
      description: "",
      tags: [""],
      restricted: false,
      parent_id: 0,
      shared_count: 1,
      error_msg: "",
      is_error: false
    };
  },

  componentDidMount() {
    var me = this;
    send_xhr("GET", "/api/resources/", localStorage.getItem("session"), null,
      function(obj) {
        me.state.all_resources = me.state.all_resources.concat(obj.data.resources);
        me.setState({
          all_resources: me.state.all_resources,
          initial_load_resources: false
        });
      },
      function(obj) {
        me.setState({initial_load_resources: false, error_msg: obj.error_msg, is_error: true});
      }
    );
  },

  render() {
    var me = this;
    var last_tag = this.state.tags[this.state.tags.length-1];
    return (
      <div>
        <Navbar setPstate={this.props.setPstate} pstate={this.props.pstate}/>

        <div className="container">
          <div className="row">
            <div className="col-md-6 col-md-offset-3">
              <form>
                <legend>New resource</legend>
                {!this.state.error_msg ? <div></div> :
                  <div className={"alert " + (this.state.is_error ? "alert-danger" : "alert-success")}>
                    <strong>{this.state.error_msg}</strong>
                  </div>
                }
                <div className="form-group">
                  <label htmlFor="resource_creator_name">Name</label>
                  <input type="text" className="form-control" id="resource_creator_name" placeholder="Name" value={this.state.name} onChange={(evt)=>this.set("name", evt.target.value)}/>
                </div>
                <div className="form-group">
                  <label htmlFor="resource_creator_description">Description</label>
                  <input type="text" className="form-control" id="resource_creator_description" placeholder="Description" value={this.state.description} onChange={(evt)=>this.set("description", evt.target.value)}/>
                </div>
                <div className="form-group">
                  <label htmlFor="resource_creator_tags">Tags</label>
                  <div className="row">
                    <div className="col-md-4">
                      {this.state.tags.slice(0, -1).map((x,i) =>
                        <ListMinusInput key={i} minusFunc={this.removeTag} value={x} index={i} editFunc={this.setTag} hasAddon={false} placeholder="Optional tag"/>
                      )}
                      <ListPlusInput addFunc={this.addTag} value={last_tag} index={this.state.tags.length-1} editFunc={this.setTag} hasAddon={true} placeholder="Optional tag"/>
                    </div>
                  </div>
                </div>
                <div className="form-group">
                  <label htmlFor="resource_creator_parent_id">Parent ID</label>
                  <select className="form-control" defaultValue={me.state.parent_id} onChange={(evt)=>this.set("parent_id", evt.target.value)}>
                    {me.state.all_resources.map(x =>
                      <option key={x.resource_id} value={x.resource_id}>{x.name}</option>
                    )}
                  </select>
                </div>
                <div className="form-group">
                  <label htmlFor="resource_creator_shared_count">Maximum simultaneous reservations (0 for unlimited)</label>
                  <input type="number" className="form-control" id="resource_creator_shared_count" placeholder="Level of Sharing" value={this.state.shared_count} onChange={(evt)=>this.set("shared_count", evt.target.value)}/>
                </div>
                <div className="checkbox">
                  <label><input type="checkbox" checked={this.state.restricted} onChange={(evt)=>this.set("restricted", evt.target.checked)}/> Restricted resource</label>
                </div>
                <div className="btn-toolbar">
                  <button type="submit" className="btn btn-primary" onClick={this.createResource} disabled={this.state.sending}>Create resource</button>
                  <button type="button" className="btn btn-default" onClick={this.cancel}>Cancel</button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    )
  }
});
