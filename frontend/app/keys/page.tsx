import { Suspense } from "react"
import { KeysPageContent } from "@/components/keys-page-content"

export default function KeysPage() {
  return (
    <Suspense fallback={null}>
      <KeysPageContent />
    </Suspense>
  )
}
