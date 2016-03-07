const ResourceCreator = React.createClass({
  createResource(evt) {
    evt.preventDefault();
    var me = this;
    this.setState({sending: true});
    send_xhr("POST", "/api/resources", localStorage.getItem("session"),
      JSON.stringify({name:this.state.name, description:this.state.description, tags: this.state.tags.filter(x => x.length > 0)}),
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

  removeTag(evt, i) {
    this.state.tags.splice(i, 1);
    this.setState({tags: this.state.tags});
  },

  getInitialState() {
    return {
      name: "",
      description: "",
      tags: [""],
      error_msg: "",
      is_error: false
    };
  },

  render() {
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
                  <input type="text" className="form-control" id="resource_creator_name" placeholder="Name" value={this.state.name} onChange={this.setName}/>
                </div>
                <div className="form-group">
                  <label htmlFor="resource_creator_description">Description</label>
                  <input type="text" className="form-control" id="resource_creator_description" placeholder="Description" value={this.state.description} onChange={this.setDescription}/>
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
