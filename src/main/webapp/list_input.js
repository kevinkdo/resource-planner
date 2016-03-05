/* Generic input for lists. Props are addFunc (called when user clicks the plus sign), value (string or number), index (int), editFunc (called onChange), placeholder (string), hasAddon (boolean)
*/
const ListPlusInput = React.createClass({
  render() {
    return (
      <div className="input-group">
        <input type="text" className="form-control" placeholder={this.props.placeholder} onChange={(evt) => this.props.editFunc(evt, this.props.index)} value={this.props.value}/>
        <span className="input-group-btn">
          <button className="btn btn-default" type="button" onClick={this.props.addFunc}><span className="glyphicon glyphicon-plus-sign" aria-hidden="true"></span></button>
        </span>
      </div>
    );
  }
});

const ListMinusInput = React.createClass({
  render() {
    return (
      <div className="input-group">
        <input type="text" className="form-control" placeholder={this.props.placeholder} onChange={(evt) => this.props.editFunc(evt, this.props.index)} value={this.props.value}/>
        <span className="input-group-btn">
          <button className="btn btn-default" type="button" onClick={(evt) => this.props.minusFunc(evt, this.props.index)}><span className="glyphicon glyphicon-minus-sign" aria-hidden="true"></span></button>
        </span>
      </div>
    );
  }
});
