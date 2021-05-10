import { Redirect, useLocation } from '@docusaurus/router'
import OriginalLayoutHead from '@theme-original/LayoutHead'
import React from 'react'

const RemoveTrailingSlashRedirect: React.FC = () => {
  const location = useLocation()
  if (location.pathname.endsWith('/')) {
    // @ts-expect-error
    return <Redirect to={location.pathname.slice(0, -1)} />
  }
  return null
}

const LayoutHead: React.FC = (props) => (
  <>
    <RemoveTrailingSlashRedirect />
    <OriginalLayoutHead {...props} />
  </>
)

export default LayoutHead
