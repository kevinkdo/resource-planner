const ResourceEditor = React.createClass({
  editResource(evt) {
    evt.preventDefault();
    var me = this;
    this.setState({sending: true});
    send_xhr("PUT", "/api/resources/" + this.props.pstate.view_id, localStorage.getItem("session"),
      JSON.stringify({restricted: this.state.restricted, name:this.state.name, description:this.state.description || "", tags: this.state.tags.filter(x => x.length > 0)}),
      function(obj) {
        me.props.setPstate({ route: "resource_list", is_error: false, error_msg: "Successfully edited resource!" });
      },
      function(obj) {
        me.setState({sending: false, error_msg: obj.error_msg, is_error: true});
      }
    );
  },

  cancel() {
    this.props.setPstate({ route: "resource_list" });
  },

  set(field, value) {
    this.state[field] = value;
    this.setState(this.state);
  },

  addTag() {
    this.state.tags.push("");
    this.setState({tags: this.state.tags});
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
      initial_load: true,
      sending: false,
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
    send_xhr("GET", "/api/resources/" + this.props.pstate.view_id, localStorage.getItem("session"), null,
      function(obj) {
        obj.data.initial_load = false;
        obj.data.tags.push("");
        me.setState(obj.data);
      },
      function(obj) {
        me.setState({initial_load: false, error_msg: obj.error_msg, is_error: true});
      }
    );
  },

  render() {
    var last_tag = this.state.tags[this.state.tags.length-1];
    return (
      <div>
        <Navbar setPstate={this.props.setPstate} pstate={this.props.pstate}/>

        <div className="container">
          <div className="row">
            <div className="col-md-6 col-md-offset-3">
              {this.state.initial_load ? <Loader /> :
                <form>
                  <legend>Edit resource {this.props.pstate.view_id}</legend>
                  {!this.state.error_msg ? <div></div> :
                    <div className={"alert " + (this.state.is_error ? "alert-danger" : "alert-success")}>
                      <strong>{this.state.error_msg}</strong>
                    </div>
                  }
                  <div className="form-group">
                    <label htmlFor="resource_editor_name">Name</label>
                    <input type="text" className="form-control" id="resource_editor_name" placeholder="Name" value={this.state.name} onChange={(evt)=>this.set("name", evt.target.value)}/>
                  </div>
                  <div className="form-group">
                    <label htmlFor="resource_editor_description">Description</label>
                    <input type="text" className="form-control" id="resource_editor_description" placeholder="Description" value={this.state.description} onChange={(evt)=>this.set("description", evt.target.value)}/>
                  </div>
                  <div className="checkbox">
                    <label><input type="checkbox" checked={this.state.restricted} onChange={(evt)=>this.set("restricted", evt.target.checked)}/> Restricted resource</label>
                  </div>
                  <div className="form-group">
                    <label htmlFor="resource_editor_tags">Tags</label>
                    <div className="row">
                      <div className="col-xs-4 col-md-4">
                        {this.state.tags.slice(0, -1).map((x,i) =>
                          <ListMinusInput key={i} minusFunc={this.removeTag} value={x} index={i} editFunc={this.setTag} hasAddon={false} placeholder="Optional tag"/>
                        )}
                        <ListPlusInput addFunc={this.addTag} value={last_tag} index={this.state.tags.length-1} editFunc={this.setTag} hasAddon={true} placeholder="Optional tag"/>
                      </div>
                    </div>
                  </div>
                  <div className="form-group">
                    <label htmlFor="resource_editor_parent_id">Parent ID</label>
                    <input type="number" className="form-control" id="resource_editor_parent_id" placeholder="Parent ID" value={this.state.parent_id} onChange={(evt)=>this.set("parent_id", evt.target.value)}/>
                  </div>
                  <div className="form-group">
                    <label htmlFor="resource_editor_shared_count">Shared Count</label>
                    <input type="number" className="form-control" id="resource_editor_shared_count" placeholder="Shared Count" value={this.state.shared_count} onChange={(evt)=>this.set("shared_count", evt.target.value)}/>
                  </div>
                  <div className="btn-toolbar">
                    <button type="submit" className="btn btn-primary" onClick={this.editResource} disabled={this.state.sending}>Save changes</button>
                    <button type="button" className="btn btn-default" onClick={this.cancel}>Go back</button>
                  </div>
                </form>
              }
            </div>
          </div>
        </div>
      </div>
    )
  }
});
