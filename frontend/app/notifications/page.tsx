import { DashboardHeader } from "@/components/dashboard-header"
import { DashboardSidebar } from "@/components/dashboard-sidebar"
import { NotificationsList } from "@/components/notifications-list"

export default function NotificationsPage() {
  return (
    <div className="flex min-h-screen bg-background">
      <DashboardSidebar />
      <div className="flex-1 flex flex-col">
        <DashboardHeader />
        <main className="flex-1 p-6 space-y-6 overflow-auto">
          <div>
            <h1 className="text-2xl font-semibold text-foreground">Notifications</h1>
            <p className="text-muted-foreground mt-1">Gérez vos notifications et alertes</p>
          </div>
          <NotificationsList />
        </main>
      </div>
    </div>
  )
}
