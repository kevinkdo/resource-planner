const ResourceEditor = React.createClass({
  editResource(evt) {
    evt.preventDefault();
    var me = this;
    this.setState({sending: true});
    send_xhr("PUT", "/api/resources/" + this.props.id, localStorage.getItem("session"),
      JSON.stringify({name:this.state.name, description:this.state.description || "", tags: this.state.tags.filter(x => x.length > 0)}),
      function(obj) {
        me.props.setPstate({ route: "resource_list" });
      },
      function(obj) {
        me.setState({sending: false, error_msg: obj.error_msg});
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

  setName(evt) {
    this.setState({name: evt.target.value});
  },

  setDescription(evt) {
    this.setState({description: evt.target.value});
  },

  setTag(evt, i) {
    this.state.tags[i] = evt.target.value;
    this.setState({tags: this.state.tags});
  },

  getInitialState() {
    return {
      initial_load: true,
      sending: false,
      name: "",
      description: "",
      tags: [""],
      error_msg: ""
    };
  },

  componentDidMount() {
    var me = this;
    send_xhr("GET", "/api/resources/" + this.props.id, localStorage.getItem("session"), null,
      function(obj) {
        obj.data.initial_load = false;
        obj.data.tags.push("");
        me.setState(obj.data);
      },
      function(obj) {
        me.setState({initial_load: false, error_msg: obj.error_msg});
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
                  <legend>Edit resource {this.props.id}</legend>
                  {!this.state.error_msg ? <div></div> :
                    <div className="alert alert-danger">
                      <strong>{this.state.error_msg}</strong>
                    </div>
                  }
                  <div className="form-group">
                    <label htmlFor="resource_editor_name">Name</label>
                    <input type="text" className="form-control" id="resource_editor_name" placeholder="Name" value={this.state.name} onChange={this.setName}/>
                  </div>
                  <div className="form-group">
                    <label htmlFor="resource_editor_description">Description</label>
                    <input type="text" className="form-control" id="resource_editor_description" placeholder="Description" value={this.state.description} onChange={this.setDescription}/>
                  </div>
                  <div className="form-group">
                    <label htmlFor="resource_editor_tags">Tags</label>
                    <div className="row">
                      <div className="col-md-4">
                        {this.state.tags.slice(0, -1).map((x,i) =>
                          <ListInput key={i} addFunc={this.addTag} value={x} index={i} editFunc={this.setTag} hasAddon={false}/>
                        )}
                        <ListInput addFunc={this.addTag} value={last_tag} index={this.state.tags.length-1} editFunc={this.setTag} hasAddon={true}/>
                      </div>
                    </div>
                  </div>
                  <div className="btn-toolbar">
                    <button type="submit" className="btn btn-primary" onClick={this.editResource} disabled={this.state.sending}>Edit resource</button>
                    <button type="button" className="btn btn-default" onClick={this.cancel}>Cancel</button>
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
