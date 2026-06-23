import PropTypes from 'prop-types';

const reactRouterDom = {
  useNavigate: jest.fn(() => jest.fn()),
  useParams: jest.fn(() => ({})),
  BrowserRouter: ({ children }) => children,
  Routes: ({ children }) => children,
  Route: ({ element }) => element,
  Link: ({ children }) => children,
  Outlet: () => null,
  Navigate: ({ to }) => <div>Redirected to {to}</div>,
};

reactRouterDom.BrowserRouter.propTypes = {
  children: PropTypes.node,
};
reactRouterDom.Routes.propTypes = {
  children: PropTypes.node,
};
reactRouterDom.Route.propTypes = {
  element: PropTypes.node,
};
reactRouterDom.Link.propTypes = {
  children: PropTypes.node,
};
reactRouterDom.Navigate.propTypes = {
  to: PropTypes.string.isRequired,
};

module.exports = reactRouterDom;
