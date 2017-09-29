import React from 'react';


let InlineError = React.createClass({
  render() {
    return(
      <div className="Inline-error">
        <div className="content">{this.props.error}</div>
      </div>
    )
  }
});

export default InlineError;
