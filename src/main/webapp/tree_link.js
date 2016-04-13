const TreeLink = React.createClass({
  getInitialState() {
    return {
      clicked: false
    };
  },

  handleClick(evt) {
    this.setState({clicked: !this.state.clicked});
    if (!this.state.clicked) {
      evt.target.setAttribute('stroke', '#5cb85c');  
    } else {
      evt.target.setAttribute('stroke', '#999');  
    }        
  },   

  handleDeleteClick(evt) {
    this.props.deleteLink(this.props.target);
    this.props.refresh();
  },

  render() {
    var line = <line className="link" stroke='#999' x1={this.props.source.x+8} y1={this.props.source.y+16} x2={this.props.target.x+8} y2={this.props.target.y} onClick={this.handleClick}></line>;
    var deleteButton = <text className="deleteButton" x={(this.props.source.x + this.props.target.x)/2 - 15} y={(this.props.source.y + this.props.target.y)/2 - 5} onClick={this.handleDeleteClick}>{this.state.clicked ? "X" : ""}</text>
    return <g>{line}{deleteButton}</g>;
  }
});