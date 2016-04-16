const TreeLink = React.createClass({
  setStroke() {
    if (this.props.is_selected) {
      return '#5cb85c';
    } else {
      return "#999";
    }
  },

  handleDeleteClick(evt) {
    this.props.deleteLink(this.props.target);
    this.props.refresh();
  },

  onLinkClick(evt) {
    this.props.setSelectedLink({source_id: this.props.source.resource_id, target_id: this.props.target.resource_id});
    evt.stopPropagation();
  },

  render() {
    var line = <line className="link" stroke={this.setStroke()} x1={this.props.source.x+8} y1={this.props.source.y+16} x2={this.props.target.x+8} y2={this.props.target.y} onClick={this.onLinkClick}></line>;
    var deleteButton = <text className="deleteButton" x={(this.props.source.x + this.props.target.x)/2 - 15} y={(this.props.source.y + this.props.target.y)/2 - 5} onClick={this.handleDeleteClick}>{this.props.is_selected ? "X" : ""}</text>
    return <g>{line}{deleteButton}</g>;
  }
});