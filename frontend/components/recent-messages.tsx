// import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
// import { Badge } from "@/components/ui/badge"
// import { Avatar, AvatarFallback } from "@/components/ui/avatar"
// import { CheckCircle2, Clock, Lock } from "lucide-react"

// const messages = [
//   {
//     id: 1,
//     sender: "Jean Dupont",
//     initials: "JD",
//     recipient: "Marie Martin",
//     status: "delivered",
//     encrypted: true,
//     time: "Il y a 5 min",
//     size: "12.4 MB",
//   },
//   {
//     id: 2,
//     sender: "Pierre Bernard",
//     initials: "PB",
//     recipient: "Sophie Leroy",
//     status: "pending",
//     encrypted: true,
//     time: "Il y a 12 min",
//     size: "8.2 MB",
//   },
//   {
//     id: 3,
//     sender: "Claire Moreau",
//     initials: "CM",
//     recipient: "Luc Petit",
//     status: "delivered",
//     encrypted: true,
//     time: "Il y a 25 min",
//     size: "15.7 MB",
//   },
//   {
//     id: 4,
//     sender: "Thomas Roux",
//     initials: "TR",
//     recipient: "Emma Simon",
//     status: "delivered",
//     encrypted: true,
//     time: "Il y a 1h",
//     size: "5.3 MB",
//   },
// ]

// export function RecentMessages() {
//   return (
//     <Card className="bg-card border-border">
//       <CardHeader className="pb-3">
//         <CardTitle className="text-lg font-semibold text-foreground">Messages Récents</CardTitle>
//       </CardHeader>
//       <CardContent className="space-y-4">
//         {messages.map((message) => (
//           <div
//             key={message.id}
//             className="flex items-center justify-between p-3 rounded-lg bg-secondary/50 hover:bg-secondary transition-colors"
//           >
//             <div className="flex items-center gap-3">
//               <Avatar className="h-10 w-10">
//                 <AvatarFallback className="bg-emerald-500/10 text-emerald-500 text-sm">
//                   {message.initials}
//                 </AvatarFallback>
//               </Avatar>
//               <div>
//                 <p className="text-sm font-medium text-foreground">
//                   {message.sender} <span className="text-muted-foreground">→</span> {message.recipient}
//                 </p>
//                 <div className="flex items-center gap-2 mt-1">
//                   <span className="text-xs text-muted-foreground">{message.time}</span>
//                   <span className="text-xs text-muted-foreground">•</span>
//                   <span className="text-xs text-muted-foreground">{message.size}</span>
//                 </div>
//               </div>
//             </div>
//             <div className="flex items-center gap-2">
//               {message.encrypted && <Lock className="w-4 h-4 text-emerald-500" />}
//               {message.status === "delivered" ? (
//                 <Badge variant="outline" className="bg-emerald-500/10 text-emerald-500 border-emerald-500/20">
//                   <CheckCircle2 className="w-3 h-3 mr-1" />
//                   Livré
//                 </Badge>
//               ) : (
//                 <Badge variant="outline" className="bg-amber-500/10 text-amber-500 border-amber-500/20">
//                   <Clock className="w-3 h-3 mr-1" />
//                   En cours
//                 </Badge>
//               )}
//             </div>
//           </div>
//         ))}
//       </CardContent>
//     </Card>
//   )
// }
