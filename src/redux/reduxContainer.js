import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';

function reduxContainer(WrappedComponent, mapStateToProps, dispatchToProps) {
  function mapDispatchToProps(dispatch) {
    return bindActionCreators(dispatchToProps, dispatch);
  }

  return connect(mapStateToProps, mapDispatchToProps)(WrappedComponent);
}

export default reduxContainer;
