const TreeNode = React.createClass({
  getFill() {
    if (this.props.is_selected) {
      return '#5cb85c';
    } else {
      return this.props.restricted ? "#d9534f": "#5bc0de";
    }
  },

  getCircleFill() {
    if (this.props.targetId == this.props.resource_id) {
      return "#5cb85c";
    }
    return this.props.restricted ? "#d9534f" : "#5bc0de";
  },

  handleDeleteClick(evt) {
    this.props.deleteNode(this.props.resource_id);
    this.props.refresh();
  },

  handleEditClick(evt) {
    this.props.setPstate({
      route: "resource_editor",
      view_id: this.props.resource_id
    });
    this.props.refresh();
  },

  handleMouseDown(evt) {
    this.props.startDrag({
      sourceId: this.props.resource_id,
      xOffset: evt.clientX - this.props.x,
      yOffset: evt.clientY - this.props.y,
      clientX: evt.clientX,
      clientY: evt.clientY
    });
  },

  handleMouseOver: function(evt) {
    this.props.setTargetId(this.props.resource_id);
  },

  handleMouseOut: function(evt) {
    this.props.setTargetId(0);
  },

  render() {
    var dragging = this.props.sourceId != 0 ? true : false;
    var me_is_dragging = this.props.sourceId == this.props.resource_id;

    var marker = this.props.can_view ? <circle r="8" fill={this.getFill()} cx={this.props.x+8} cy={this.props.y+8} onMouseDown={this.handleMouseDown} onMouseUp={this.handleMouseUp}/> : <ellipse className="nopointer noselect" cx={this.props.x+8} cy={this.props.y+8} rx={9} ry={7} fill={this.props.can_view ? "none": "purple"} />;
    var circle = <circle className={dragging && !me_is_dragging ? "ghostCircle show" : "ghostCircle noshow"} r="30" cx={this.props.x + 8} cy={this.props.y + 8} opacity="0.4" fill={this.getCircleFill()} onMouseOver={this.handleMouseOver} onMouseOut={this.setBlueFill} onMouseOut={this.handleMouseOut} />;
    var text = <text className="nodelabel nopointer noselect" x={this.props.x + 20} y={this.props.y + 13}>{this.props.name}</text>;
    var deleteButton = <text className="deleteButton noselect" x={this.props.x - 15} y={this.props.y + 13} onClick={this.handleDeleteClick}>{this.props.is_selected && this.props.can_view ? "X" : ""}</text>;
    var editButton = <text className="editButton noselect" x={this.props.x - 45} y={this.props.y + 13} onClick={this.handleEditClick}>{this.props.is_selected && this.props.can_view ? "edit" : ""}</text>;
    return <g className={me_is_dragging ? "draggable nopointer" : "draggable yespointer"}>{marker}{circle}{deleteButton}{editButton}{text}</g>;
  }
});