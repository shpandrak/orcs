import React from 'react';


var Description = React.createClass({
  render(){
    return(
      <div className={`${this.props.className}__description`} style={{ height: this.props.plansCount*42 }}>
        {this.props.text}
      </div>
    )
  }
});

Description.propTypes = {
  text: React.PropTypes.string.isRequired,
  className: React.PropTypes.string.isRequired
}

export default Description;
