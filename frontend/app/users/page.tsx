import { Suspense } from "react"
import { UsersPageContent } from "@/components/users-page-content"

export default function UsersPage() {
  return (
    <Suspense fallback={null}>
      <UsersPageContent />
    </Suspense>
  )
}
