/* Generic input for lists. Props are addFunc (called when user clicks the plus sign), value (string or number), index (int), editFunc (called onChange), placeholder (string), hasAddon (boolean)
*/
const ListInput = React.createClass({
  render() {
    var add_on = !this.props.hasAddon ? null :
      <span className="input-group-btn">
        <button className="btn btn-default" type="button" onClick={this.props.addFunc}><span className="glyphicon glyphicon-plus-sign" aria-hidden="true"></span></button>
      </span>;

    return (
      <div className="input-group">
        <input type="text" className="form-control" placeholder={this.props.placeholder} onChange={(evt) => this.props.editFunc(evt, this.props.index)} value={this.props.value}/>
        {add_on}
      </div>
    );
  }
});